define(['jquery', 'flow', 'baguetteBox'], function ($, Flow, BaguetteBox) {
    // global
    var index = 0;
    var loadFlag = false;
    var anchor = 0;
    var imgListField = $('#imgListField');
    var loadingField = $('#loadingField');

    var grid = Flow.createGrid(imgListField);

    var init = function () {
        $('#homeLink').attr('href', getHost());
        loadPage(true);

        loadingField.click(function () {
            // showItem(loadingField);
            loadPage(false);
        });

        // page back
        $(window).bind("popstate", function () {
            index = 0;
            loadFlag = false;
            stopLoadPage();
            BaguetteBox.hide();
            showItem(loadingField);

            reRenderPage();
        });

        // window resize
        var resizeTm = null;
        $(window).resize(function () {
            if (resizeTm != null) {
                clearTimeout(resizeTm);
            }
            resizeTm = setTimeout(function () {
                layoutMainPage();
            }, 200);
        });

        // page scroll
        var pageNav = $('#pageNav');
        var footer = $('#footer');
        $(window).scroll(function () {
            if (pageNav.offset().top > 0) {
                pageNav.get(0).classList.remove('page-nav-flat');
                pageNav.get(0).classList.add('page-nav-cascade');
            } else {
                pageNav.get(0).classList.remove('page-nav-cascade');
                pageNav.get(0).classList.add('page-nav-flat');
            }

            // alert($(window).height() + '-' + footer.offset().top);
            // if (Math.ceil($(window).height() + $(document).scrollTop()) >= Math.ceil($(document).height())) {
            //     showItem(loadingField);
            //     loadPage(false);
            // }
        });
    };

    /**
     * clear image list, load from server or history state
     */
    var reRenderPage = function () {
        var state = history.state;
        if (!state) {
            loadPage(true);
        } else {
            renderPage(0, state.data, state.path, false, true);
        }
    };

    var loadPage = function (clear) {
        var params = getUrlParams();
        if (!params.hasOwnProperty('p')) {
            getPage(index, 20, '', false, clear);
        } else {
            getPage(index, 20, decodeURI(params['p']).replace(/\\/g, '\\\\'), false, clear);
        }
    };

    var getPage = function (start, size, path, modUrl, clear) {
        index = start;
        $(document).attr("title", path);
        var encodePath = encodeURI(path);
        $.ajax({
            url: './api/files?path=' + encodePath + '&s=' + start + '&c=' + size,
            type: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                renderPage(start, data, encodePath, modUrl, clear);
            }
        });
    };

    var renderPage = function (start, data, encodePath, modUrl, clear) {
        var historyData = {
            path: encodePath,
            data: data,
            anchor: anchor,
            index: index
        };

        if (modUrl && clear) {          // load new list
            setUrlParam('p', encodePath);
        }

        if (clear) {                    // refresh list
            genPagePosition(encodePath);
            // genSideNav(encodePath);
            imgListField.html('<div class="grid-sizer" id="gridSizer"></div><div class="gutter-sizer"></div>');

            Flow.reload(grid);
        } else {                        // append list
            var currentState = history.state;
            if (currentState) {
                historyData.data = currentState.data.concat(historyData.data);
            }
        }

        var gridSizer = $('#gridSizer');

        loadFlag = true;
        if (data.length < 20) {
            hiddenItem(loadingField);
        } else {
            showItem(loadingField);
        }

        // dir first
        var i;
        for (i = 0; i < data.length; ++i) {
            if (!loadFlag) {
                break;
            }

            if (data[i].type === 2) {
                item = $('<a href="javascript:void(0);" id="img-' + data[i].src + '" ></a>');
                item.click(function () {
                    var idval = $(this).attr('id').replace('img-', '');
                    getPage(0, 20, idval.replace(/\\/g, '\\\\'), true, true);
                });
                item.append($('<div class="card grid-item"><div class="card-body">' +
                    '<h6 class="card-title">' + data[i].fileName + '</h6></div>'));

                Flow.append(grid, item);
            }
        }

        for (i = 0; i < data.length; ++i) {
            if (!loadFlag) {
                break;
            }
            var item;
            if (data[i].type === 1) {
                if (data[i].extraInfo !== '') {
                    var extraInfo = JSON.parse(data[i].extraInfo);

                    var w = gridSizer.width();
                    var h = w * extraInfo.smHeight / extraInfo.smWidth;

                    item = $('<div class="card grid-item">' +
                        '<a href="/img' + data[i].src + '?raw=1"><img class="card-img thumbnail-img" alt="' +
                        data[i].fileName + '" title="' + data[i].fileName + '" width="' + w + '" height="' + h +
                        '" src="/img' + data[i].src + '" id="img-' + (index + i) + '">' + '</a></div>');

                    Flow.append(grid, item);
                }
            }
        }
        index += data.length;
        historyData.index = index;
        history.replaceState(historyData, '', '');

        layoutMainPage();

        // remove specify size for image auto layout after
        $('.thumbnail-img').removeAttr('width').removeAttr('height');

        BaguetteBox.run('.gallery', {
            noScrollbars: true,
            preload: 3,
            async: true,
            onChange: function (currentIndex, imagesCount) {
                $('#baguetteBox-figure-' + currentIndex).click(function () {
                    BaguetteBox.hide();
                });
            }
        });
    };

    var genPagePosition = function (encodePath) {
        var pagePos = $('#pagePos');
        pagePos.html('<li><a href="' + getHost() + '" class="breadcrumb-item" id="homeLink">Home</a></li>');

        var secPath = '';
        var sec = decodeURI(encodePath).split('/');
        for (var i = 0; i < sec.length; ++i) {
            if (i === sec.length - 1) {
                pagePos.append($('<li class="breadcrumb-item active">' + sec[i] + '</li>'));
            } else {
                if (sec[i] !== '') {
                    secPath += '/';
                }
                secPath += sec[i];
                console.log(secPath);
                var link = $('<a id="breadcrumb-' + secPath +
                    '" href="javascript:void(0);">' + sec[i] + '</a>');
                link.click(function () {
                    var idval = $(this).attr('id').replace('breadcrumb-', '');
                    getPage(0, 20, idval.replace(/\\/g, '\\\\'), true, true);
                });

                var item = $('<li class="breadcrumb-item"></li>');
                item.append(link);
                pagePos.append(item);
            }
        }
    };

    var layoutMainPage = function () {
        Flow.layout(grid);
    };

    return {
        init: init,
        reRenderPage: reRenderPage,
        loadPage: loadPage,
        getPage: getPage,
        renderPage: renderPage,
        genPagePosition: genPagePosition,
        layoutMainPage: layoutMainPage
    }
});