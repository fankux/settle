import React from 'react';
import {
    AutoComplete, Button, Layout,
    Slider, Space, Upload, Input, Popover
} from 'antd';
import {
    CloudUploadOutlined,
    CheckOutlined,
    DownOutlined,
} from '@ant-design/icons';
import './Action.css'
import {SliderValue} from "antd/lib/slider";

const {Header} = Layout;

class UploadAction extends React.Component {
    render() {
        const uploadProps = {
            name: 'file',
            action: 'https://upload-to-backend',
            headers: {
                authorization: 'authorization-text',
            },
            onChange(info) {
                if (info.file.status !== 'uploading') {
                    console.log(info.file, info.fileList);
                }
                if (info.file.status === 'done') {
                    // message.success(`${info.file.name} file uploaded successfully`);
                } else if (info.file.status === 'error') {
                    // message.error(`${info.file.name} file upload failed.`);
                }
            },
        };
        return (
            <Upload {...uploadProps}>
                <Button type={"primary"}>
                    <CloudUploadOutlined/>上传
                </Button>
            </Upload>
        );
    }
}

type ScaleSwicherProp = {
    onAfterChange?: (value: SliderValue) => void,
}

class ScaleSwicher extends React.Component<ScaleSwicherProp> {
    onAfterChange = (value: SliderValue) => {
        console.log(value);
    }

    render() {
        const content = (
            <div style={{width: 100}}>
                <Slider step={20} defaultValue={60}
                        onAfterChange={this.onAfterChange}/>
            </div>
        );
        return (
            <Popover placement="bottom" content={content}>
                <Button ghost><DownOutlined/>缩放</Button>
            </Popover>
        );
    }
}

class SearchBox extends React.Component {
    render() {
        return (
            <AutoComplete
                dropdownClassName="certain-category-search-dropdown"
                dropdownMatchSelectWidth={500}
                style={{width: 250}}
                // options={options}
            >
                <Input.Search size="large" placeholder="input here"/>
            </AutoComplete>
        );
    }
}

export class Action extends React.Component {
    render() {
        return (
            <Header className="site-layout-head">
                <Space>
                    <UploadAction/>
                    <Button ghost><CheckOutlined/>选择</Button>
                    <ScaleSwicher/>
                </Space>
            </Header>
        );
    }
}
