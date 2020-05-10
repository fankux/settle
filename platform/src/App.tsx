import React from 'react';
import {Layout} from 'antd';
import {Nav} from './content/Nav'
import {Action} from './content/Action'
import {Gallery} from './content/Gallery';
import './App.css'

export default class App extends React.Component {
    render() {
        return (
            <Layout style={{minHeight: '100vh'}}>
                <Nav/>
                <Layout className="site-layout">
                    <Action/>
                    <Layout className="site-layout-body" hasSider={true}>
                        <Gallery/>
                    </Layout>
                    {/*<Footer style={{textAlign: 'center'}}>Settle ©2020 Created by Fankux</Footer>*/}
                </Layout>
            </Layout>
        );
    }
}
