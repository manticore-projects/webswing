import $ from "jquery";
import { loadTranslations } from "./webswing-translate";

const baseUrl = (window as any).webswingRequestBaseUrl ? ((window as any).webswingRequestBaseUrl + '/') : '';
loadTranslations(baseUrl).then((translations) => {
    const translate = (key: string) => translations.translate(key);

    showPartial(window.location.href, $('#webswing-content'), (window as any).webswingPartialHtml);

    function showPartial(url: string, element: any, html: any) {
        element.html(translate(html));
        const form = element.find('form').first();
        if (form != null) {
            form.submit((event: any) => {
                loadPartial(url, element, form.serialize());
                event.preventDefault();
            });
        }
    }

    function loadPartial(url: string, element: any, data: any) {
        if (typeof element === 'function') {
            element = element();
        }
        $.ajax({
            xhrFields: {
                withCredentials: true
            },
            type: 'POST',
            url,
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            data,
            complete: (xhr) => {
                const response = xhr.responseText;
                let loginMsg: any = {};
                if (response != null) {
                    try {
                        loginMsg = JSON.parse(response);
                    } catch (error) {
                        if (baseUrl) {
                            window.top!.location.href = baseUrl;
                            return;
                        } else {
                            element.html(translate("<p>Invalid request</p>"))
                        }
                    }
                    if (loginMsg.redirectUrl != null) {
                        window.top!.location.href = loginMsg.redirectUrl;
                    } else if (loginMsg.partialHtml != null) {
                        showPartial(url, element, loginMsg.partialHtml);
                    } else {
                        element.html(translate("<p>${login.unexpectedError}</p>"));
                    }
                } else {
                    element.html(translate("<p>${login.serverNotAvailable}</p>"));
                }
            }
        });
    }

}).catch((e) => {
    console.error("Failed to load Translations", e);
})