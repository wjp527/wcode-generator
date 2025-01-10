import { Flex, Upload } from 'antd';
import React, { useState } from 'react';
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import type { UploadProps } from 'antd';

import { uploadFileUsingPost } from '@/services/backend/fileController';
import '@/index.css';
import { COS_HOST } from '@/constants';

interface Props {
  biz: string;
  onChange?: (url: string) => void;
  value?: string;
}

/**
 * 文件上传组件
 */

const PictureUploader: React.FC<Props> = (Props) => {
  // 解构
  const { biz, value, onChange } = Props;
  // 设置loading
  const [loading, setLoading] = useState(false);
  // 上传配置
  const uploadProps: UploadProps = {
    name: 'file',
    multiple: false,
    listType: 'picture-card',
    maxCount: 1,
    disabled: loading,
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
        // 上传成功后的回调
        fileObj.onSuccess(res);

        // 拼接完整路径
        const fullPath = COS_HOST + res.data;
        // 上传成功后的回调
        onChange?.(fullPath);
        // 关闭loading
        setLoading(false);
      } catch (error: any) {
        console.log('上传失败: ' + error.message);
        // 上传失败后的回调
        fileObj.onError(error);
        // 关闭loading
        setLoading(false);
      }
    },
  };

  /**
   * 上传按钮
   */
  const uploadButton = (
    <button style={{ border: 0, background: 'none' }} type="button">
      {loading ? <LoadingOutlined /> : <PlusOutlined />}
      <div style={{ marginTop: 8 }}>上传</div>
    </button>
  );

  return (
    <Flex justify="center" gap={16}>
      <Upload {...uploadProps}>
        {value ? <img src={value} alt="图片" className="w-full" /> : uploadButton}
      </Upload>
    </Flex>
  );
};
export default PictureUploader;
