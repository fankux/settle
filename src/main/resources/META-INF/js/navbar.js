define(['jquery', 'velocity', 'gallery'], function ($, Velocity, Gallery) {
    var dirTree = {};

    var init = function () {
        // toggle sidebar
        toogleBar($('#sidebarToggler'), $('#sidebar'), true);
        toogleBar($('#asidebarToggler'), $('#aside'), false);
    };

    /**
     * left bar
     * @param toogler
     * @param bar
     * @param isleft
     */
    var toogleBar = function (toogler, bar, isleft) {
        toogler.click(function () {
            if (bar.css("display") === 'none') { // open
                bar.css('display', 'block');
                bar.velocity({translateX: isleft ? [0, '-100%'] : 0}, {
                    duration: 300,
                    complete: function () {
                        Gallery.layoutMainPage();
                    }
                });
            } else { // close
                bar.velocity({translateX: isleft ? '-100%' : ['100%', 0]}, {
                    duration: 300,
                    complete: function () {
                        bar.css('display', 'none');
                        Gallery.layoutMainPage();
                    }
                });
            }
        });
    };

    function genSideNav(encodePath) {
        var sec = decodeURI(encodePath).split('/');

        var currentSidePos = $('#currentSidePos');
        currentSidePos.html('');

        var arrowOff = '<svg class="align-middle" x="0px" y="0px" width="20px" height="20px" viewBox="0 0 20 20"' +
        ' focusable="false" fill="#000000"><polygon points="8,5 13,10 8,15"></polygon></svg>';

        var arrowOpen = '<svg x="0px" y="0px" width="20px" height="20px" viewBox="0 0 20 20" focusable="false"' +
        ' fill="#000000"><polygon points="5,8 10,13 15,8"></polygon></svg>';

        var arrowActive = '<svg x="0px" y="0px" width="20px" height="20px" viewBox="0 0 20 20" focusable="false"' +
        ' fill="#000000" style="fill: rgb(66, 133, 244);"><polygon points="5,8 10,13 15,8"></polygon></svg>';

        var secPath = '';
        for (var i = 0; i < sec.length; ++i) {
            if (sec[i].trim() === '') {
                continue;
            }

            var active = '';
            var arrow = '';
            var arrowClass = '';
            var dirLinkHrefNavi;
            if (i === sec.length - 1) {
                active = 'active';
                arrow = arrowActive;
                arrowClass = '';
                dirLinkHrefNavi = 'javascript:void(0);';
            } else {
                arrow = arrowOpen;
                dirLinkHrefNavi = 'javascript:getPage(0, 20, \'' + secPath.replace(/\\/g, '\\\\') + '\', true, true);';
            }

            var navitem = $('<div class="navbar-nav flex-row w-100">' +
            '<a class="nav-link sidebar-arrow ' + arrowClass + '" href="javascript:toggleDir(this)">' +
            arrow + '</a>' + '<a class="nav-link sidebar-item ' + active + '" href="' +
            dirLinkHrefNavi + '">' + sec[i] + '</a>' + '</div>');
            currentSidePos.append(navitem);

            navitem = $('<nav class="navbar flex-column w-100 align-items-start sidebar-nav-nest"></nav>');
            currentSidePos.append(navitem);

            currentSidePos = navitem;
            secPath += '/' + sec[i];
        }
    }

    function getDirTree(path) {
        var sec = path.split('/');
        var secPath = '';
        for (var i = 0; i < sec.length; ++i) {

        }
    }

    function getDir(path, treeNode) {
        var secPath = '';
        var sec = path.split('/');
        for (var i = 0; i < sec.length; ++i) {
            if (!treeNode.hasOwnProperty(sec[i])) {
                treeNode[secPath] = {};

                var encodePath = encodeURI(secPath);
                $.ajax({
                    url: './api/dirs?path=' + encodePath,
                    type: 'POST',
                    dataType: 'json',
                    contentType: 'application/json',
                    success: function (data) {
                        treeNode[secPath] = data;
                    }
                });
            }

            secPath += '/' + sec[i];
        }
    }

    var toggleDir = function (it) {
        var subNav = $('#' + it);
        subNav.velocity('slideUp');

    };
    return {
        init: init,
        toggleDir: toggleDir
    }
});
