import { Util, getParam, checkCookie, getImageString } from "./webswing-util"
import { loadTranslations } from "./webswing-translate";

loadTranslations().then(
    (translations) => {
        (window as any).selectorLogout = logout;

        const translate = (key: string) => translations.translate(key);
        const util = Util(translations);
        if (!checkCookie()) {
            $('#webswing-content').html(translate('${dialog.cookiesDisabledDialog}'));
            return;
        }

        window.addEventListener('storage', (event) => {
            if (event.key === 'webswingLogout') {
                location.reload();
            }
        });

        const login = util.webswingLogin;
        let user: any;
        const loginData = {
            securityToken: getParam("token"),
            realm: getParam("realm")
        };
        const url = window.location.href;
        const baseUrl = url.substring(0, Math.min(url.indexOf('#'), url.indexOf('?')));
        login(baseUrl, $('#webswing-content'), loginData, loadApps);

        // @ts-ignore
        function loadApps(data: any, request: any) {
            localStorage.setItem("webswingLogin", Date.now().toString());

            user = request.getResponseHeader('webswingUsername');
            $.ajax({
                xhrFields: {
                    withCredentials: true
                },
                type: 'GET',
                url: 'rest/apps',
                beforeSend: (xhr) => {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + util.getToken());
                },
                success: (result) => {
                    loadAdminConsoleAccess((showAdmin: any) => {
                        if (showAdmin) {
                            loadAdminConsoleUrl((adminConsoleUrl: any) => {
                                if (adminConsoleUrl && adminConsoleUrl.length) {
                                    show(result, true, adminConsoleUrl);
                                } else {
                                    show(result, false);
                                }
                            });
                        } else {
                            show(result, false);
                        }
                    });
                }
            });
        }

        function loadAdminConsoleUrl(callback: any) {
            $.ajax({
                xhrFields: {
                    withCredentials: true
                },
                type: 'GET',
                url: 'rest/adminConsoleUrl',
                beforeSend: (xhr) => {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + util.getToken());
                },
                success(data) {
                    callback(data);
                },
                error() {
                    callback(false);
                }
            });
        }

        function loadAdminConsoleAccess(callback: any) {
            $.ajax({
                xhrFields: {
                    withCredentials: true
                },
                type: 'GET',
                url: 'rest/adminConsoleAccess',
                beforeSend: (xhr) => {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + util.getToken());
                },
                success: (data) => {
                    callback(data && JSON.parse(data));
                },
                error: () => {
                    callback(false);
                }
            });
        }

        function show(apps: any, showAdmin: boolean, adminConsoleUrl?: string) {
            let header: any = '<h1 class="ws-selector-title">${selector.welcome} <span>' + user + '</span>${selector.message}</h1>';
            const links = '${selector.lang} '+(showAdmin ? ' <a href="' + adminConsoleUrl + '" id="admin">${selector.admin}</a> | ' : '')
                 + '<a href="javascript: selectorLogout();" id="logout">${selector.logout}</a>';
            let content;
            $('#commonDialog').addClass('ws-selector')
            if (apps == null || apps.length === 0) {
                header = null;
                content = '<p id="commonDialog-title">${selector.noApp}</p>';
            } else {
                content = '<div class="ws-selector-container">';
                let counter = 0;
                for (const i in apps) {
                    if (apps.hasOwnProperty(i)) {
                        const app = apps[i];
                        content += '<div class="ws-selector-btn">'
                            + '<a href="' + app.url + '" role="button" aria-labelledby="selector-btn-' + counter + '">'
                            + '<img src="' + getImageString(app.base64Icon) + '" class="ws-selector-btn-thumb"/>'
                            + '<div id="selector-btn-' + counter + '" class="ws-selector-btn-label">' + app.name + '</div>'
                            + '</a></div>';
                        counter++;
                    }
                }
                content += '</div>';
            }
            $('#webswing-header').html(translate(header));
            $('#webswing-links').html(translate(links));
            $('#webswing-content').html(translate(content));
        }

        function logout() {
            $.ajax({
                xhrFields: {
                    withCredentials: true
                },
                type: 'GET',
                url: 'logout',
                beforeSend: (xhr) => {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + util.getToken());
                }
            }).done(() => {
                localStorage.setItem("webswingLogout", Date.now().toString());
                location.reload();
            });
        }
    }
).catch((e) => {
    console.error("Failed to load Translations", e);
})

