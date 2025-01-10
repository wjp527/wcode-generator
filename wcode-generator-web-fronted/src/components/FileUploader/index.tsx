import { Flex } from 'antd';
import React, { useState } from 'react';
import { InboxOutlined } from '@ant-design/icons';
import type { UploadProps } from 'antd';

import Dragger from 'antd/es/upload/Dragger';
import { uploadFileUsingPost } from '@/services/backend/fileController';
import '@/index.css';
import { UploadFile } from 'antd/lib';

interface Props {
  // 类型 【用户头像/生成器图片/生成器产物包/生成器制作模板文件】
  biz: string;
  // 上传成功后的回调
  onChange?: (fileList: UploadFile[]) => void;
  // 文件列表
  value?: UploadFile[];
  // 描述
  description?: string;
}

/**
 * 文件上传组件
 */

const FileUploader: React.FC<Props> = (Props) => {
  // 解构
  const { biz, value, description, onChange } = Props;
  // 设置loading
  const [loading, setLoading] = useState(false);
  // 上传配置
  const uploadProps: UploadProps = {
    name: 'file',
    // 是否支持多选
    multiple: false,
    // 文件列表类型
    listType: 'text',
    // 最大上传数量
    maxCount: 1,
    // 文件列表
    fileList: value,
    // 是否禁用
    disabled: loading,
    // 文件上传成功后的回调
    onChange({ fileList }) {
      console.log(fileList, 'fileList');
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
          // 上传的文件
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
    <Flex justify="center" gap={16} className="w-full ">
      <Dragger {...uploadProps} className="w-full">
        <p className="ant-upload-drag-icon w-full">
          <InboxOutlined className="w-full flex justify-center items-center" />
        </p>
        <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
        <p className="ant-upload-hint">{description}</p>
      </Dragger>
    </Flex>
  );
};
export default FileUploader;
