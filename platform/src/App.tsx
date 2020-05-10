import React from 'react';
import {Layout} from 'antd';
import {Gallery} from './content/Gallery';
import {Nav} from './content/Nav'
import './App.css'

const {Header} = Layout;

export default class App extends React.Component {
    render() {
        return (
            <Layout style={{minHeight: '100vh'}}>
                <Nav/>
                <Layout className="site-layout">
                    <Header className="site-layout-head" style={{padding: 0}}/>
                    <Layout className="site-layout-body">
                        <Gallery/>
                    </Layout>
                    {/*<Footer style={{textAlign: 'center'}}>Settle ©2020 Created by Fankux</Footer>*/}
                </Layout>
            </Layout>
        );
    }
}
