import React, { useEffect, useRef, useState } from 'react';
import type { FormInstance, FormProps, SelectProps } from 'antd';
import { Button, Form, Input, message, Select } from 'antd';
import '@/index.css';
import PictureUploader from '@/components/PictureUploader';
import { getLoginUserUsingGet, updateMyUserUsingPost } from '@/services/backend/userController';
import { useModel } from '@umijs/max';
type FieldType = {
  userName?: string;
  tags?: string;
  userProfile?: string;
  userAvatar?: string;
  remember?: string;
};

const UserCenter: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};
  const formRef = useRef<FormInstance>(null);

  // 初始化
  const init = async () => {
    let res = await getLoginUserUsingGet();
    if (res.code === 0) {
      if (res.data?.tags) {
        res.data.tags = JSON.parse(res.data.tags);
      }
      formRef.current?.setFieldsValue(res.data);
    } else {
      message.error('获取用户信息失败');
    }
  };
  const onFinish: FormProps<FieldType>['onFinish'] = async (values) => {
    values.tags = JSON.stringify(values.tags);
    let res = await updateMyUserUsingPost(values);
    if (res.code === 0) {
      message.success('更新成功');
      // 刷新页面
      init();
      window.location.reload();
    } else {
      message.error('更新失败：' + res.message);
    }
  };
  const onFinishFailed: FormProps<FieldType>['onFinishFailed'] = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  useEffect(() => {
    init(); // 组件加载时获取用户信息
  }, []);

  return (
    <div className="container w-full">
      <div className="flex justify-center align-center w-full">
        <Form
          ref={formRef}
          name="basic"
          // labelCol={{ span: 8 }}
          // wrapperCol={{ span: 16 }}
          style={{ maxWidth: 1200, minWidth: 800 }}
          onFinish={onFinish}
          onFinishFailed={onFinishFailed}
          autoComplete="off"
          // 怎么将数据展示出来
          initialValues={currentUser}
        >
          <Form.Item<FieldType> label="用户名" name="userName">
            <Input />
          </Form.Item>
          {/* 怎么输入tabs标签 */}
          <Form.Item<FieldType> label="用户标签" name="tags">
            <Select
              mode="tags"
              allowClear
              style={{ width: '100%' }}
              placeholder="Please select"
              // options={options}
            />
          </Form.Item>
          <Form.Item<FieldType> label="用户头像" name="userAvatar">
            <PictureUploader biz="user_avatar" />
          </Form.Item>
          <Form.Item<FieldType> label="用户简介" name="userProfile">
            <Input.TextArea />
          </Form.Item>
          <Form.Item label={null}>
            <Button type="primary" htmlType="submit">
              保存
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
};
export default UserCenter;
