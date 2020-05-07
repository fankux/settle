import React from "react";
import {Layout, Menu} from "antd";
import {
    DesktopOutlined,
    PieChartOutlined,
    TeamOutlined,
    UserOutlined,
} from '@ant-design/icons';

const {Sider} = Layout;
const {SubMenu} = Menu;

export class Nav extends React.Component {
    defaultWidth: number = 80;

    state = {
        arrow: true, // false: left to right; right: right to left
        collapsedState: 1, // 0.collapsed; 1.semi; 2.show
        width: this.defaultWidth,
    };

    onCollapse = (collapsed) => {
        if (this.state.collapsedState === 0) {
            console.log('show');
            this.setState({
                arrow: false,
                collapsedState: 2,
                width: this.defaultWidth
            });
        } else if (this.state.collapsedState === 1) {
            console.log('collpased');
            this.setState({
                arrow: false,
                collapsedState: 0,
                width: 0
            });
        } else if (this.state.collapsedState === 2) {
            console.log('semi');
            this.setState({
                arrow: true,
                collapsedState: 1,
                width: this.defaultWidth
            });
        }
    };

    lastState = {};
    timeHandler;
    onMouseEnter = () => {
        this.timeHandler = setTimeout(() => {
            this.lastState = this.state;
            this.setState({
                arrow: false,
                collapsedState: 2,
                width: this.defaultWidth,
            });
        }, 600);
    }

    onMouseLeave = () => {
        clearTimeout(this.timeHandler)
        this.setState(this.lastState);
    }

    render() {
        return (
            <Sider collapsible
                   reverseArrow={this.state.arrow}
                   defaultCollapsed={true}
                   collapsed={this.state.collapsedState !== 2}
                   collapsedWidth={this.state.width}
                   onCollapse={this.onCollapse}>
                <div className="logo"/>
                <div onMouseEnter={this.onMouseEnter} onMouseLeave={this.onMouseLeave}>
                    <Menu theme="dark" defaultSelectedKeys={['1']} mode="inline">
                        <Menu.Item key="1" icon={<PieChartOutlined/>}>
                            Option 1
                        </Menu.Item>
                        <Menu.Item key="2" icon={<DesktopOutlined/>}>
                            Option 2
                        </Menu.Item>
                        <SubMenu key="sub1" icon={<UserOutlined/>} title="User">
                            <Menu.Item key="3">Tom</Menu.Item>
                            <Menu.Item key="4">Bill</Menu.Item>
                            <Menu.Item key="5">Alex</Menu.Item>
                        </SubMenu>
                        <SubMenu key="sub2" icon={<TeamOutlined/>} title="Team">
                            <Menu.Item key="6">Team 1</Menu.Item>
                            <Menu.Item key="8">Team 2</Menu.Item>
                        </SubMenu>
                    </Menu>
                </div>
            </Sider>
        );
    }
}