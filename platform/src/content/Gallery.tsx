import React from 'react';
import {Card, Col, Drawer, Dropdown, Menu, Row} from 'antd';
import {EllipsisOutlined} from '@ant-design/icons';
import './Gallery.css'
import SubMenu from "antd/es/menu/SubMenu";
import 'photoswipe/dist/photoswipe.css'
import 'photoswipe/dist/default-skin/default-skin.css'
import * as PhotoSwipe from 'photoswipe/dist/photoswipe.min'
import * as PhotoSwipeUI_Default from 'photoswipe/dist/photoswipe-ui-default.min'

type LightBoxProps = {
    images: any,
    currentId: number;
}

class LightBox extends React.Component<LightBoxProps> {
    render() {
        return (
            <div className="pswp" tabIndex={-1} role="dialog" aria-hidden="true">
                <div className="pswp__bg"/>
                <div className="pswp__scroll-wrap">
                    <div className="pswp__container">
                        <div className="pswp__item"/>
                        <div className="pswp__item"/>
                        <div className="pswp__item"/>
                    </div>
                    <div className="pswp__ui pswp__ui--hidden">
                        <div className="pswp__top-bar">
                            <div className="pswp__counter"/>
                            <button className="pswp__button pswp__button--close" title="Close (Esc)"/>
                            <button className="pswp__button pswp__button--share" title="Share"/>
                            <button className="pswp__button pswp__button--fs" title="Toggle fullscreen"/>
                            <button className="pswp__button pswp__button--zoom" title="Zoom in/out"/>
                            <div className="pswp__preloader">
                                <div className="pswp__preloader__icn">
                                    <div className="pswp__preloader__cut">
                                        <div className="pswp__preloader__donut"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="pswp__share-modal pswp__share-modal--hidden pswp__single-tap">
                            <div className="pswp__share-tooltip"/>
                        </div>
                        <button className="pswp__button pswp__button--arrow--left" title="Previous (arrow left)">
                        </button>
                        <button className="pswp__button pswp__button--arrow--right" title="Next (arrow right)">
                        </button>
                        <div className="pswp__caption">
                            <div className="pswp__caption__center"/>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

type ImageMeta = {
    id: number,
    title: string,
    src: string
}

type ImageCardProps = {
    meta: ImageMeta,
    index: number,
    onShowImageDrawer: any,
    onShowLightbox: any,
}

type ImageDrawerProps = {
    imageDrawerVisible: boolean,
    onCloseImageDrawer: any,
    images: any,
    currentId: number;
}

class ImageDrawer extends React.Component<ImageDrawerProps> {
    onClose = () => {
        this.props.onCloseImageDrawer();
    }

    render() {
        const img = this.props.images[this.props.currentId];
        return (
            <div>
                {this.props.currentId === 0 ? '' :
                    <Drawer
                        title={img.title} placement="right" mask={true}
                        visible={this.props.imageDrawerVisible}
                        onClose={this.onClose}
                        closable={true}
                        keyboard={true}
                        destroyOnClose={true}
                    >
                    </Drawer>
                }
            </div>
        );
    }
}

class ImageCard extends React.Component<ImageCardProps> {
    onShowDrawer = (id) => {
        this.props.onShowImageDrawer(id);
    }

    onShowLightBox = (index) => {
        this.props.onShowLightbox(index);
    }

    menu = (
        <Menu>
            <Menu.Item key="1">文件中打开</Menu.Item>
            <Menu.Item key="2">重命名</Menu.Item>
            <Menu.Item key="3">删除</Menu.Item>
            <Menu.Item key="4">属性</Menu.Item>
            <Menu.Divider/>
            <SubMenu title="更多">
                <Menu.Item>转发</Menu.Item>
                <Menu.Item>TODO</Menu.Item>
            </SubMenu>
        </Menu>
    );

    render() {
        return (
            <Dropdown overlay={this.menu} trigger={['contextMenu']}>
                <Card className="image-card" hoverable={true} bordered={false}
                      size="small" style={{width: 240}}
                      title={this.props.meta.title}
                      extra={<EllipsisOutlined
                          key="ellipsis"
                          onClick={this.onShowDrawer.bind(this, this.props.meta.id)}/>}
                >
                    <img style={{width: 240}} alt={this.props.meta.title}
                         onClick={this.onShowLightBox.bind(this, this.props.index)}
                         src={this.props.meta.src}/>
                </Card>
            </Dropdown>
        )
    }
}

export class Gallery extends React.Component {
    state = {
        count: 10,
        drawerVisible: false,
        currentImageId: 0
    };

    showImageDrawer = (id) => {
        this.setState({
            drawerVisible: true,
            currentImageId: id
        });
    };

    closeImageDrawer = () => {
        this.setState({
            drawerVisible: false,
        });
    };

    // TODO... request to server
    imageSrc = 'https://os.alipayobjects.com/rmsportal/QBnOOoLaAfKPirc.png';
    items = {
        '1': {id: '1', title: '1111111', src: this.imageSrc, osrc: this.imageSrc, ow: 240, oh: 300},
        '2': {id: '2', title: '2222222', src: this.imageSrc, osrc: this.imageSrc, ow: 240, oh: 300},
        '3': {id: '3', title: '3333333', src: this.imageSrc, osrc: this.imageSrc, ow: 240, oh: 300},
    };

    initLightbox = () => {
        let self = this;
        // build items array
        return Object.keys(this.items).map(function (key) {
            const img = self.items[key];
            return {
                src: img.osrc,
                w: img.ow,
                h: img.oh
            }
        });
    }
    lightboxItems = this.initLightbox();

    showLightbox = (index: number) => {
        // define options (if needed)
        let options = {
            index: index, // start at first slide
            loop: false,
            closeOnScroll: false,
            preload: [2, 2],
            bgOpacity: 0.9,
            history: false,
            shareEl: false,
        };
        let pswpElement = document.querySelectorAll('.pswp')[0];
        let photoswipe = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, this.lightboxItems, options);
        photoswipe.init(index);
    }

    render() {
        const self = this;
        let idx = 0;
        return ([
            <div key="rows" id="gallery" className="gallery" style={{padding: 24, minHeight: 360}}>
                <Row gutter={[8, 8]}>
                    {Object.keys(this.items).map(function (key) {
                        return <Col key={key}>
                            <ImageCard meta={self.items[key]} index={idx++}
                                       onShowImageDrawer={self.showImageDrawer}
                                       onShowLightbox={self.showLightbox}/></Col>;
                    })}
                </Row>
            </div>,
            <LightBox key="lightbox" images={this.items} currentId={this.state.currentImageId}/>,
            <ImageDrawer key="imageDrawer" images={this.items}
                         currentId={this.state.currentImageId}
                         onCloseImageDrawer={this.closeImageDrawer}
                         imageDrawerVisible={this.state.drawerVisible}/>

        ]);
    }
}