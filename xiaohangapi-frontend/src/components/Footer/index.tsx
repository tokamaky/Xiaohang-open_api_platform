import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import '@umijs/max';
import React from 'react';

const Footer: React.FC = () => {
    const defaultMessage = 'Xiaohang Ji';
    const currentYear = new Date().getFullYear();
    return (
        <DefaultFooter
            style={{
                background: 'none',
            }}
            copyright={`${currentYear} ${defaultMessage}`}
            links={[
                {
                    key: 'Ant Design Pro',
                    title: 'Panda API',
                    href: 'http://localhost:8000',
                    blankTarget: true,
                },
                {
                    key: 'github',
                    title: <GithubOutlined />,
                    href: 'https://github.com/tokamaky',
                    blankTarget: true,
                },
            ]}
        />
    );
};
export default Footer;
