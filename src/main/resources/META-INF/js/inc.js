function getUrlParams() {
    var params = {};
    var hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for (var i = 0; i < hashes.length; i++) {
        if (hashes[i].indexOf('=') === -1) {
            continue;
        }
        hash = hashes[i].split('=');
        params[hash[0]] = hash[1];
    }
    return params;
}

function setUrlParam(key, value) {
    var params = getUrlParams();
    params[key] = value;

    var query = '?';
    for (var k in params) {
        query += k + '=' + params[k] + '&';
    }
    query = query.substr(0, query.length - 1);
    history.pushState(null, '', query);
}

function getUrlPath() {
    var path = window.location.href.replace('http://', '');
    path = path.replace('https://', '');
    var idxes = path.split('/');
    if (idxes.length <= 1) {
        return '';
    }
    return path.replace(idxes[0], '');
}

function getHost() {
    var path = window.location.href.replace('http://', '');
    path = path.replace('https://', '');
    var idxes = path.split('/');
    return 'http://' + idxes[0];
}

function stopLoadPage() {
    if (window.stop !== undefined) {
        window.stop();
    }
    else if (document.execCommand !== undefined) {
        document.execCommand("Stop", false);
    }
}

function hiddenItem(item) {
    var style = item.attr('class');
    item.attr('class', style + ' hide');
}

function showItem(item) {
    var style = item.attr('class');
    item.attr('class', style.replace(/hide/g, ''));
}