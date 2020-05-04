import React from 'react';
import {Card, Col, Drawer, Dropdown, Menu, Row} from 'antd';
import {EllipsisOutlined} from '@ant-design/icons';
import './Gallery.css'
import SubMenu from "antd/es/menu/SubMenu";

type ImageMeta = {
    id: number,
    title: string,
    src: string
}

type ImageCardProps = {
    meta?: ImageMeta,
    onShowImageDrawer?: any,
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
    onShow = (id) => {
        this.props.onShowImageDrawer(id);
    }

    menu = (
        <Menu>
            <Menu.Item key="1">文件中打开</Menu.Item>
            <Menu.Item key="2">重命名</Menu.Item>
            <Menu.Item key="3">删除</Menu.Item>
            <Menu.Item key="4">属性</Menu.Item>
            <Menu.Divider />
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
                      title={this.props.meta?.title}
                      extra={<EllipsisOutlined
                          key="ellipsis"
                          onClick={this.onShow.bind(this, this.props.meta?.id)}/>}
                >
                    <img style={{width: 240}} alt={this.props.meta?.title}
                         src={this.props.meta?.src}/>
                </Card>
            </Dropdown>
        )
    }
}

export class Gallery extends React.Component<ImageCardProps> {
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
        '1': {id: '1', title: '1111111', src: this.imageSrc},
        '2': {id: '2', title: '2222222', src: this.imageSrc},
        '3': {id: '3', title: '3333333', src: this.imageSrc},
    };

    render() {
        const self = this;
        return ([
            <div id="gallery" className="gallery" style={{padding: 24, minHeight: 360}}>
                <Row gutter={[8, 8]}>
                    {Object.keys(this.items).map(function (key) {
                        return <Col key={key}>
                            <ImageCard meta={self.items[key]}
                                       onShowImageDrawer={self.showImageDrawer}/></Col>;
                    })}
                </Row>
            </div>,
            <ImageDrawer images={this.items}
                         currentId={this.state.currentImageId}
                         onCloseImageDrawer={this.closeImageDrawer}
                         imageDrawerVisible={this.state.drawerVisible}/>
        ]);
    }
}