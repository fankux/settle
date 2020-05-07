import React from 'react';
import {Layout} from 'antd';
import {Gallery} from './content/Gallery';
import {Nav} from './content/Nav'
import './App.css'

const {Header, Content, Footer} = Layout;

export default class App extends React.Component {
    render() {
        return (
            <Layout style={{minHeight: '100vh'}}>
                <Nav/>

                <Layout className="site-layout">
                    <Header className="site-layout-background" style={{padding: 0}}/>
                    <Content style={{margin: '0 16px'}}>
                        <Gallery/>
                    </Content>

                    <Footer style={{textAlign: 'center'}}>Settle ©2020 Created by Fankux</Footer>
                </Layout>
            </Layout>
        );
    }
}
