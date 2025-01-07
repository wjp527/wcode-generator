import { Flex } from 'antd';
import React, { useState } from 'react';
import { InboxOutlined } from '@ant-design/icons';
import type { UploadProps } from 'antd';

import Dragger from 'antd/es/upload/Dragger';
import { uploadFileUsingPost } from '@/services/backend/fileController';
import '@/index.css';
import { UploadFile } from 'antd/lib';

interface Props {
  biz: string;
  onChange?: (fileList: UploadFile[]) => void;
  value?: UploadFile[];
  description?: string;
}

/**
 * 文件上传组件
 */

const FileUploader: React.FC<Props> = (Props) => {
  const { biz, value, description, onChange } = Props;

  const [loading, setLoading] = useState(false);
  const uploadProps: UploadProps = {
    name: 'file',
    multiple: false,
    listType: 'text',
    maxCount: 1,
    fileList: value,
    disabled: loading,
    onChange({ fileList }) {
      onChange?.(fileList);
    },
    // 文件上传
    customRequest: async (fileObj: any) => {
      try {
        setLoading(true);
        const res = await uploadFileUsingPost(
          {
            biz,
          },
          {},
          fileObj.file,
        );
        fileObj.onSuccess(res);
        setLoading(false);
      } catch (error: any) {
        console.log('上传失败: ' + error.message);
        fileObj.onError(error);
        setLoading(false);
      }
    },
  };

  return (
    <Flex justify="center" gap={16}>
      <Dragger {...uploadProps}>
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
        <p className="ant-upload-hint">{description}</p>
      </Dragger>
    </Flex>
  );
};
export default FileUploader;
