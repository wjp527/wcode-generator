import { Button, Card, Flex, Divider, Spin } from 'antd';
import React, { useState } from 'react';
import { InboxOutlined } from '@ant-design/icons';
import type { UploadProps } from 'antd';

import Dragger from 'antd/es/upload/Dragger';
import {
  testDownloadFileUsingGet,
  testUploadFileUsingPost,
} from '@/services/backend/fileController';
import { COS_HOST } from '@/constants';
import '@/index.css';
import { saveAs } from 'file-saver';
/**
 * 文件上传/下载测试
 */

const TestFilePage: React.FC = () => {
  const [value, setValue] = useState('');

  const [loading, setLoading] = useState(false);
  const props: UploadProps = {
    name: 'file',
    multiple: false,
    maxCount: 1,
    // 文件上传
    customRequest: async (fileObj: any) => {
      try {
        setLoading(true);
        const res = await testUploadFileUsingPost({}, fileObj.file);
        fileObj.onSuccess(res);
        setValue(res.data || '');
        setLoading(false);
      } catch (error: any) {
        console.log('上传失败: ' + error.message);
        fileObj.onError(error);
        setLoading(false);
      }
    },
    // 文件移除
    onRemove: () => {
      setValue('');
    },
  };

  return (
    <Flex>
      <Card title="文件上传">
        <Dragger {...props}>
          <p className="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p className="ant-upload-text">Click or drag file to this area to upload</p>
          <p className="ant-upload-hint">
            Support for a single or bulk upload. Strictly prohibited from uploading company data or
            other banned files.
          </p>
        </Dragger>
      </Card>
      <Card title="文件下载">
        <div>文件地址: {COS_HOST + (value || '')}</div>
        <Divider />
        {/* 展示loading */}
        <Spin spinning={loading} tip="Loading...">
          <img src={COS_HOST + (value || '')} alt="" className="h-80 mb-4" />
        </Spin>
        <Button
          type="primary"
          onClick={async () => {
            const blob = await testDownloadFileUsingGet(
              {
                filepath: value,
              },
              {
                // 返回文件流
                responseType: 'blob',
              },
            );

            // 使用 file-saver 来保存文件
            const fullPath = COS_HOST + (value || '');
            // 获取文件名
            // 截取文件路径中最后一个 / 后面的字符串
            const fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            saveAs(blob, fileName);
          }}
        >
          下载
        </Button>
      </Card>
    </Flex>
  );
};
export default TestFilePage;
