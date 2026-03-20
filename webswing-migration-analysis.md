# WebSwing JDK 21+ Migration Analysis

## Executive Summary

After a thorough review of the `webswing-directdraw`, `webswing-app-toolkit`, `webswing-app-toolkit-java11`, and `webswing-app-services` modules, here is a complete picture of the upgrade surface and a prioritised action plan.

**Good news:** The directdraw rendering protocol itself (protobuf) is completely JDK-version-independent. The TypeScript canvas renderer (`webswing-dd.ts`) is clean, well-structured code with no JDK coupling. The problems are confined to two distinct areas: the JS build toolchain and internal JDK API usage in the Java modules.

---

## Part 1: JavaScript Build Modernization (Likely Root Cause of Rendering Failure)

### Current State

| Component | Current Version | Status |
|-----------|----------------|--------|
| Node.js minimum | ≥10.16.0 | Ancient |
| Webpack | 4.23.1 | EOL |
| TypeScript | 3.8.3 | Very old (current: 5.x) |
| Babel | 7.6 with `plugin-proposal-*` | Deprecated plugin names |
| babel-loader | 8.0.6 | Old |
| `awesome-typescript-loader` | 5.2.1 | In devDeps, deprecated 2019 |
| `tslint` | 5.11 | Deprecated (replaced by ESLint) |
| `webpack-merge` | 4.2.2 | Old API |
| `copy-webpack-plugin` | 5.1.1 | Old API |
| Target | IE11 (in .babelrc) | No longer needed |
| `protobufjs` | 6.8.8 | Functional but old |
| `npm-run-all` | 4.1.5 | OK |
| Build integration | `frontend-maven-plugin` | OK |

### Why This Probably Breaks Rendering

The build pipeline uses `babel-loader` with `@babel/preset-typescript` to transpile `.ts` → `.js`, then Webpack 4 bundles it as a UMD module. On a modern Node.js (18+), several things can go wrong silently:

1. **`webpack-merge` v4 API changed in v5** — the merge function signature changed from `merge(a, b)` to `{ merge }` named export
2. **`copy-webpack-plugin` v5 syntax** uses array directly instead of `{ patterns: [...] }`  
3. **Old Babel proposal plugins** may emit warnings or subtly different output on newer Node
4. **`trash-cli` v1.4** (used in `clean` script) may fail silently on modern Node
5. **`protobufjs` 6.8.8 CLI tools** (`pbjs`, `pbts`) may fail during the `proto` build step

If `npm run build` fails or produces a malformed bundle, the existing `dist/main.js` (committed to the repo) might be stale — built with an older toolchain and not matching the current proto definition or TypeScript source.

### Recommended Modernized Stack

| Component | Target Version | Notes |
|-----------|---------------|-------|
| Node.js | ≥18 | LTS |
| Webpack | 5.x | Or consider esbuild for much faster builds |
| TypeScript | 5.x | |
| `ts-loader` | 9.x | Replace babel-loader for TS (simpler, faster) |
| `webpack-merge` | 6.x | New API: `const { merge } = require('webpack-merge')` |
| `copy-webpack-plugin` | 12.x | New API: `new CopyPlugin({ patterns: [...] })` |
| `protobufjs` | 7.x | Or stay on 6.11 (latest 6.x) |
| Linting | ESLint + @typescript-eslint | Replace tslint |
| Target | ES2020+ | Drop IE11 |

### Files to Change

1. **`package.json`** — update all dependency versions, update scripts, bump engines
2. **`webpack.config.js`** — replace `babel-loader` with `ts-loader`, update `CopyPlugin` syntax, remove `tslint-loader`, remove jQuery `ProvidePlugin` if not needed
3. **`webpack-prd.config.js`** — update `webpack-merge` import syntax
4. **`webpack-dev.config.js`** — update `webpack-merge` import, update `devServer` config (Webpack 5 changed `contentBase` → `static`)
5. **`tsconfig.json`** — update target to `ES2020`, add `skipLibCheck: true`
6. **`.babelrc`** — delete (no longer needed with ts-loader)
7. **`tslint.json`** — delete (replace with eslint if desired)
8. **`src/main/webapp/proto/dd.js`** — regenerate with updated `pbjs`
9. **`src/main/webapp/proto/dd.d.ts`** — regenerate with updated `pbts`

### Source Code Changes in `webswing-dd.ts`

The TypeScript source is clean. No API changes needed — the code uses standard Canvas 2D APIs and protobufjs decode. Only thing to verify:

- `Promise` usage with untyped `resolve()` calls (lines 984, 986, 1015, 1017) — TypeScript 5 strict mode may require `resolve(undefined)` or better generic typing
- The `prepareImages` and `initializeFontFaces` methods use `new Promise` with untyped resolve — may need minor type annotations

---

## Part 2: Java Internal API Dependencies

### Direct Draw Module (`webswing-directdraw-swing`)

| File | Import | Risk on JDK 21+ |
|------|--------|-----------------|
| `DirectDrawUtils.java` | `sun.java2d.SunGraphics2D` | **Medium** — still present in JDK 21-25, needs `--add-exports` |
| `DirectDrawUtils.java` | `sun.java2d.loops.FontInfo` | **Medium** — same |
| `WebImage.java` | `sun.awt.image.SurfaceManager` | **Medium** — present but API may shift |
| `WebImage.java` | `sun.java2d.SurfaceData` | **Medium** — same |
| `VolatileWebImageWrapper.java` | `sun.awt.image.SurfaceManager` | **Medium** — same |
| `VolatileWebImageWrapper.java` | `sun.java2d.SurfaceData` | **Medium** — same |
| `GlyphKeyConst.java` | `sun.font.GlyphList` | **HIGH** — GlyphList internals have changed |
| `GlyphListConst.java` | `sun.font.GlyphList` + `sun.java2d.loops.FontInfo` | **HIGH** — same |

### App Toolkit Java11 Module

| File | Import | Risk on JDK 21+ |
|------|--------|-----------------|
| `WebToolkit11.java` | `sun.awt.SunToolkit`, `sun.awt.LightweightFrame`, `sun.awt.datatransfer.DataTransferer`, `sun.awt.image.SurfaceManager`, `sun.java2d.SurfaceData` | **Medium** — all still present |
| `WebFontConfiguration.java` | `sun.awt.FontConfiguration`, `sun.font.SunFontManager` | **Medium-High** — font config internals may have changed |
| `WebFontManager.java` | `sun.awt.FontConfiguration`, `sun.font.SunFontManager` | **Medium-High** — same |
| `WebGraphicsEnvironment11.java` | `sun.awt.FontConfiguration` | **Medium** |
| `WebDataTransfer.java` | `sun.awt.datatransfer.DataTransferer`, `sun.awt.datatransfer.ToolkitThreadBlockedHandler` | **Medium** |

### App Toolkit Module

| File | Import | Risk on JDK 21+ |
|------|--------|-----------------|
| `WebFontPeer.java` | `sun.awt.PlatformFont` | **Medium** |
| `WebWindowPeer.java` | `sun.java2d.InvalidPipeException` | **Low** — still present |
| `AbstractEventDispatcher.java` | `sun.awt.UngrabEvent` | **Low** — still present |
| `WebToolkitThreadBlockedHandler.java` | `sun.awt.datatransfer.ToolkitThreadBlockedHandler` | **Medium** |

### App Services Module

| File | Import | Risk on JDK 21+ |
|------|--------|-----------------|
| `SwingClassloader.java` | `sun.security.util.SecurityConstants` | **HIGH** — `SecurityManager` deprecated for removal in JDK 17, `SecurityConstants.ALL_PERMISSION` may be removed |
| `AccessibilityUtil.java` | `com.sun.java.accessibility.util.*` | **Medium** — accessibility bridge still exists |

### Current `--add-exports` Coverage

The POMs already declare these (in `webswing-directdraw/pom.xml` and `webswing-app-services/pom.xml`):

```
--add-exports=java.desktop/sun.awt=ALL-UNNAMED
--add-exports=java.desktop/sun.awt.dnd=ALL-UNNAMED
--add-exports=java.desktop/sun.awt.dnd.peer=ALL-UNNAMED
--add-exports=java.base/sun.nio.cs=ALL-UNNAMED
--add-exports=java.desktop/sun.java2d=ALL-UNNAMED
--add-exports=java.desktop/sun.java2d.pipe=ALL-UNNAMED
--add-exports=java.desktop/sun.java2d.loops=ALL-UNNAMED
--add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED
--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED
--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED
--add-exports=java.desktop/java.awt.dnd=ALL-UNNAMED
--add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED
--add-exports=java.desktop/sun.font=ALL-UNNAMED
--add-exports=java.desktop/sun.print=ALL-UNNAMED
--add-exports=java.base/sun.security.util=ALL-UNNAMED
```

**Missing at compile time** (needed but not listed):
```
--add-exports=java.desktop/sun.java2d=ALL-UNNAMED  (already present)
```

**Needed at RUNTIME** (in addition to compile-time, need `--add-opens` for reflective access):
```
--add-opens=java.desktop/sun.java2d=ALL-UNNAMED
--add-opens=java.desktop/sun.awt.image=ALL-UNNAMED
--add-opens=java.desktop/sun.font=ALL-UNNAMED
--add-opens=java.desktop/sun.awt=ALL-UNNAMED
```

### Critical JDK 21+ Breakage Points

1. **`sun.security.util.SecurityConstants`** in `SwingClassloader.java` line 500: Uses `SecurityConstants.ALL_PERMISSION`. With SecurityManager deprecated for removal, this will break. Replace with `new java.security.AllPermission()`.

2. **`sun.font.GlyphList`** in `GlyphKeyConst.java` and `GlyphListConst.java`: The internal font rasterization API has undergone changes. Need to check if the `GlyphList` API methods used still exist in JDK 21+.

3. **Compiler source/target level**: The `pom.xml` sets `<source>11</source><target>11</target>` with `maven-compiler-plugin` 3.1 (ancient). Should be updated to at least `<release>11</release>` syntax (or `21` if dropping older JDK support), and the plugin version should be 3.13+.

---

## Part 3: Recommended Migration Order

### Phase 1: JS Build (Do This First)
This is the most likely fix for the rendering failure. Can be done in a single focused session.

1. Modernize `package.json` dependencies
2. Rewrite webpack configs for Webpack 5
3. Replace babel-loader with ts-loader
4. Regenerate proto bindings (`dd.js`, `dd.d.ts`)
5. Rebuild and verify `dist/main.js` output
6. Test in browser — if rendering works, the JS build was the problem

### Phase 2: SecurityConstants Fix
Quick fix, high impact:

1. Replace `SecurityConstants.ALL_PERMISSION` → `new AllPermission()` in `SwingClassloader.java`
2. Remove the `import sun.security.util.SecurityConstants` 
3. Remove the corresponding `--add-exports=java.base/sun.security.util=ALL-UNNAMED`

### Phase 3: Runtime `--add-opens` Flags  
Ensure the application startup script passes all needed `--add-opens` flags for JDK 21+.

### Phase 4: GlyphList / Font API Audit
The most complex Java change — verify `sun.font.GlyphList` API compatibility with JDK 21/23/25 and adapt `GlyphKeyConst`/`GlyphListConst` if methods have changed.

### Phase 5: Maven Plugin Updates
- `maven-compiler-plugin` 3.1 → 3.13+
- Use `<release>11</release>` instead of `<source>/<target>`
- `frontend-maven-plugin` — verify latest version

### Phase 6 (Future): Jetty Migration
Not needed for JDK 21+ compatibility, but needed for the `webswing-server` module modernization. This is the largest effort and should be done after the above is working.

---

## What I Need from You Next

To start **Phase 1** (JS build modernization) right now, I just need you to confirm:

1. What Node.js version do you have installed? (`node --version`)
2. When you run `cd webswing-directdraw/webswing-directdraw-javascript && npm run build`, what errors do you see?
3. Is the current `dist/main.js` the file that gets deployed, or does the Maven build regenerate it?

For **Phase 2-4**, I can work on those in parallel — I have all the source code I need from the uploaded zips.
