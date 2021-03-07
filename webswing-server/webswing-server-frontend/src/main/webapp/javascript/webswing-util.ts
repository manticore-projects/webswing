import { Translations } from "./webswing-translate";

interface ILoginData {
    securityToken?: string,
    realm?: string,
    successUrl?: string
}

interface ILoginResponse {
    partialHtml?: string
    redirectUrl?: string
}

let token: string | null;

export function Util(translations: Translations) {

    const translate = (key: string) => translations.translate(key);

    return {
        webswingLogin,
        webswingLogout,
        refreshLogin,
        getToken
    }

    function webswingLogin(baseUrl: string, element: (() => JQuery<HTMLElement>) | JQuery<HTMLElement>, loginData: ILoginData | string, successCallback: (data: any, request: JQuery.jqXHR) => void, failedCallback?: () => void) {
        if (token == null) {
        	// try refresh token first
        	refreshLogin(baseUrl, () => {
                // continue with login, doesn't matter if we got the token or not, or if there was an error
                doWebswingLogin(baseUrl, element, loginData, successCallback, failedCallback);
            });
        } else {
        	doWebswingLogin(baseUrl, element, loginData, successCallback, failedCallback);
        }
    }
    
    function doWebswingLogin(baseUrl: string, element: (() => JQuery<HTMLElement>) | JQuery<HTMLElement>, loginData: ILoginData | string, successCallback: (data: any, request: JQuery.jqXHR) => void, failedCallback?: () => void) {
    	$.ajax({
        	beforeSend: (xhr) => {
                xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
                if (token != null && token.length) {
                	xhr.setRequestHeader('Authorization', 'Bearer ' + token);
                }
            },
            xhrFields: {
                withCredentials: true
            },
            type: 'POST',
            url: baseUrl + 'login',
            contentType: typeof loginData === 'object' ? 'application/json' : 'application/x-www-form-urlencoded; charset=UTF-8',
            data: typeof loginData === 'object' ? JSON.stringify(loginData) : loginData,
            timeout: 7000,
            success: (data, _, request) => {
            	if (request.responseText) {
                    saveTokens(request.responseText);
            	}
                if (successCallback != null) {
                    successCallback(data, request);
                }
            },
            error: (xhr) => {
                const response = xhr.responseText;
                let elementResolved: JQuery<HTMLElement>;

                if (response != null) {
                    // resolve this only if needed because it updates dialog view
                	if (typeof element === 'function') {
                        elementResolved = element();
                	} else {
                        elementResolved = element;
                    }

                    let loginMsg: ILoginResponse = {};
                    try {
                        loginMsg = JSON.parse(response);
                    } catch (error) {
                            console.error(error);
                            if (failedCallback != null) {
                                failedCallback();
                            }
                            return;
                    }
                    if (loginMsg.redirectUrl != null) {
                        window.top.location.href = loginMsg.redirectUrl;
                    } else if (loginMsg.partialHtml != null) {
                        elementResolved.html(translate(loginMsg.partialHtml));
                        const form = elementResolved.find('form').first();
                        form.submit((event) => {
                            elementResolved.find('#progress').show();
                            webswingLogin(baseUrl, elementResolved, form.serialize(), successCallback);
                            event.preventDefault();
                        });
                    } else {
                            console.error("WebswingLogin: Unexpected response:"+JSON.stringify(loginMsg));
                            if (failedCallback != null) {
                                failedCallback();
                            }
                    }
                } else {
                	if (failedCallback != null) {
                		failedCallback();
                	} else {
                		if (typeof element === 'function') {
                            elementResolved = element();
                        } else {
                            elementResolved = element;
                        }

                		elementResolved.html(translate("<p>${login.serverNotAvailable}</p>"));
                	}
                }
            }
        });
    }

    function webswingLogout(baseUrl: string, element: (() => JQuery<HTMLElement>) | JQuery<HTMLElement>, doneCallback: () => void, failedCallback: () => void, tabLogout?: boolean) {
    	const oldToken = token;

        $.ajax({
            type: 'GET',
            url: baseUrl + 'logout',
            beforeSend: (xhr) => {
                xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
                if (oldToken != null && oldToken.length) {
                	xhr.setRequestHeader('Authorization', 'Bearer ' + oldToken);
                }
            },
            xhrFields: {
                withCredentials: true
            },
            timeout: 7000
        }).always((_, _1, xhr) => {
            if (!tabLogout) {
                localStorage.setItem("webswingLogout", Date.now().toString());
            }

            const response = (typeof xhr === 'string') ? null : xhr.responseText;
            let elementResolved: JQuery<HTMLElement>;
            if (typeof element === 'function') {
                elementResolved = element();
            } else {
                elementResolved = element;
            }
            if (response != null) {
                clearToken();

                let loginMsg: ILoginResponse = {};
                try {
                    loginMsg = JSON.parse(response);
                } catch (error) {
                    console.error(error);
                    failedCallback();
                        return;
                }
                if (loginMsg.redirectUrl != null) {
                    window.top.location.href = loginMsg.redirectUrl;
                        return;
                } else if (loginMsg.partialHtml != null) {
                    elementResolved.html(translate(loginMsg.partialHtml));
                } else {
                        console.error("WebswingLogin: Unexpected response:"+JSON.stringify(loginMsg));
                    doneCallback();
                }
            } else {
                failedCallback();
            }
        });
    }

    function refreshLogin(baseUrl: string, doneCallback?: (success: boolean) => void) {
        $.ajax({
            beforeSend: (xhr) => {
                xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
            },
            xhrFields: {
                withCredentials: true
            },
            type: 'POST',
            url: baseUrl + "rest/refreshToken",
            timeout: 7000,
            complete: (request, textStatus) => {
            	let success = false;
                if (textStatus === "success" && request.responseText) {
                	saveTokens(request.responseText);
                	success = true;
                }

                if (doneCallback) {
                    doneCallback(success);
                }
            }
        });
    }

    function saveTokens(response: string) {
        const result = JSON.parse(response);
        token = result.accessToken;
    }

    function clearToken() {
        token = null;
    }
    
}

export function getToken() {
    return token || "";
}

export function getParam(name: string) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    const results = new RegExp("[\\?&]" + name + "=([^&#]*)").exec(location.href);
    return results == null ? undefined : decodeURIComponent(results[1]);
}

export function isArrayBufferSupported() {
    if ('ArrayBuffer' in window && ArrayBuffer.toString().indexOf("[native code]") !== -1) {
        return true;
    }
    return false;
}

export function detectIE() {
    const ua = window.navigator.userAgent;

    const msie = ua.indexOf('MSIE ');
    if (msie > 0) {
        // IE 10 or older => return version number
        return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
    }

    const trident = ua.indexOf('Trident/');
    if (trident > 0) {
        // IE 11 => return version number
        const rv = ua.indexOf('rv:');
        return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
    }

    const edge = ua.indexOf('Edge/');
    if (edge > 0) {
        // IE 12 => return version number
        return parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
    }
    // other browser
    return false;
}

export function detectChrome() {
    return /Chrome/.test(navigator.userAgent) && /Google Inc/.test(navigator.vendor);
}

export function detectMac() {
    return navigator.platform.toUpperCase().indexOf('MAC') >= 0;
}


export function checkCookie() {
    // Quick test if browser has cookieEnabled host property
    if (navigator.cookieEnabled) {
        return true;
    }
    // Create cookie
    document.cookie = "cookietest=1";
    const ret = document.cookie.indexOf("cookietest=") !== -1;
    // Delete cookie
    document.cookie = "cookietest=1; expires=Thu, 01-Jan-1970 00:00:01 GMT";
    return ret;
}

export function getImageString(bytes: Uint8Array) {
    let data;
    if (typeof bytes === 'string') {
        data = bytes;
    } else if (typeof bytes === 'object') {
        let binary = '';
        for (let i = 0, l = bytes.byteLength; i < l; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        data = window.btoa(binary);
    }
    return 'data:image/png;base64,' + data;
}


export function isIOS() {
    const platforms = [
        'iPad Simulator',
        'iPhone Simulator',
        'iPod Simulator',
        'iPad',
        'iPhone',
        'iPod'
    ];
    return (platforms.indexOf(navigator.platform) !== -1)
        // iPad on iOS 13 detection
        || (navigator.userAgent.indexOf("Mac") !== -1 && "ontouchend" in document)
}

export function GUID() {
    const S4 = () => {
        return Math.floor(Math.random() * 0x10000).toString(16);
    };
    return (S4() + S4() + S4());
}

export function detectFF() {
    return navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
}

export function createCookie(name: string, value: string, days: number) {
    let expires;

    if (days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    } else {
        expires = "";
    }
    document.cookie = escape(name) + "=" + escape(value) + expires + "; path=/";
}

export function readCookie(name: string) {
    const nameEQ = escape(name) + "=";
    const ca = document.cookie.split(';');
    for (let c of ca) {
        while (c.charAt(0) === ' ') {
            c = c.substring(1, c.length);
        }
        if (c.indexOf(nameEQ) === 0) {
            return unescape(c.substring(nameEQ.length, c.length));
        }
    }
    return null;
}

export function eraseCookie(name: string) {
    createCookie(name, "", -1);
}

export function getDpr() {
    return Math.ceil(window.devicePixelRatio) || 1;
}


export function isTouchDevice() {
    return 'ontouchstart' in window;
}

export function getTimeZone() {
    const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

    if (timeZone) {
        return timeZone;
    }

    // IE, get offset in minutes
    return getTimeZoneFromOffset(new Date().getTimezoneOffset());
}

export function fixConnectionUrl(connectionUrl: string) {
    // change relative URL to full URL
    if (connectionUrl.toLowerCase().indexOf('http') !== 0) { // if relative url
        const host = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
        let path = connectionUrl;
        if (path.indexOf('/') !== 0) {// if relative path
            const currentPath = document.location.pathname;
            if (currentPath.lastIndexOf('/') === currentPath.length - 1 ) { // current path ends with /
                path = currentPath + path;
            } else { // otherwise remove the path after last /
                path = currentPath.substring(0,currentPath.lastIndexOf('/') + 1) + path;
            }
        }
        connectionUrl = host + path;
    }
    return connectionUrl;
}

function getTimeZoneFromOffset(offset: number) {
    return "GMT" + ((offset < 0 ? '+' : '-') + // Note the reversed sign!
        pad(Math.abs(offset / 60), 2) +
        pad(Math.abs(offset % 60), 2));
}

function pad(value: number, length: number) {
    let str = "" + value;
    while (str.length < length) {
        str = '0' + str;
    }
    return str;
}
