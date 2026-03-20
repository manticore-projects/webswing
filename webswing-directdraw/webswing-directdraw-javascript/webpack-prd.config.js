const path = require("path");
const { merge } = require("webpack-merge");
const commonConfig = require("./webpack.config.js");
const ROOT = path.resolve(__dirname, "src/main/webapp");

module.exports = merge(commonConfig, {
  context: ROOT,

  mode: "production",
  output: {
    library: "webswing-directdraw-javascript",
    libraryTarget: "umd",
    globalObject: 'typeof self !== "undefined" ? self : this'
  },
});
