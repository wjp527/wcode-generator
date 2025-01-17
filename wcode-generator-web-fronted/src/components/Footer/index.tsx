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
              <GithubOutlined /> wjp
            </>
          ),
          href: 'https://github.com/wjp527',
          blankTarget: true,
        },
        {
          key: 'github',
          title: (
            <>
              <div>
                <span>备案号</span>
                <span>苏ICP备2025155289号-1</span>
              </div>
            </>
          ),
          href: 'https://beian.miit.gov.cn/',
          blankTarget: true,
        },
      ]}
    />
  );
};
export default Footer;
