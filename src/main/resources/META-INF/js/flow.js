define(['require', 'jquery', 'masonry', 'bridget'],
function (require, $, Masonry) {
    // require jquery-bridget, it's included in masonry.pkgd.js
    $.bridget('masonry', Masonry);

    var createGrid = function (it) {
        return it.masonry({
            itemSelector: '.grid-item',
            columnWidth: '.grid-sizer',
            gutter: '.gutter-sizer',
            horizontalOrder: true
        });
    };

    var reload = function (grid) {
        grid.masonry('reloadItems');
    };

    var layout = function (grid) {
        grid.masonry('layout');
    };

    var append = function (grid, item) {
        grid.masonry().append(item).masonry('appended', item);
    };

    return {
        createGrid: createGrid,
        reload: reload,
        layout: layout,
        append: append
    }
});