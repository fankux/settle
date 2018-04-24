require.config({
    baseUrl: "",
    paths: {
        'jquery': 'webjars/jquery/jquery.min',
        'bridget': 'webjars/jquery-bridget/jquery-bridget',
        'velocity': 'webjars/velocity/velocity.min',
        'masonry': 'webjars/masonry/masonry.pkgd.min',
        'imagesloaded': 'webjars/imagesloaded/imagesloaded.pkgd.min',
        'bootstrap': 'webjars/bootstrap/js/bootstrap.bundle.min',
        'baguetteBox': 'js/baguetteBox.min',
        'gallery': 'js/gallery',
        'navbar': 'js/navbar',
        'flow': 'js/flow'
    },
    shim: {
        'bootstrap': {
            deps: ['jquery']
        },
        'velocity': {
            deps: ['jquery']
        },
        'baguetteBox': {}
    }
});

require(
[
    'gallery',
    'navbar',
    'imagesloaded',
    'bootstrap'
],

function (Gallery, Navbar) {
    Gallery.init();
    Navbar.init();
});
