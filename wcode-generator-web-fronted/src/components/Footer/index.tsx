import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import '@umijs/max';
import React from 'react';

const Footer: React.FC = () => {
  const defaultMessage = 'wjp';
  const currentYear = new Date().getFullYear();
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright={`${currentYear} ${defaultMessage}`}
      links={[ 
        {
          key: 'github',
          title: (
            <>
              <GithubOutlined /> wjp源码
            </>
          ),
          href: 'https://github.com/wjp527',
          blankTarget: true,
        },
      ]}
    />
  );
};
export default Footer;
