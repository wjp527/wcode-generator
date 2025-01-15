import { useEffect, useState } from 'react';
import {
  downloadGeneratorByIdUsingGet,
  getGeneratorVoByIdUsingGet,
} from '@/services/backend/generatorController';
import { DownloadOutlined, EditOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useParams, history, useModel, Link } from '@umijs/max';
import { Card, Col, Image, message, Row, Space, Tag, Typography, Button, Tabs, Alert } from 'antd';
import dayjs from 'dayjs';
import type { TabsProps } from 'antd';

import '@/index.css';
import { COS_HOST } from '@/constants';
import FileConfig from './components/FileConfig';
import AuthorInfo from './components/AuthorInfo';
import ModelConfig from './components/ModelConfig';
import { saveAs } from 'file-saver';
/**
 * 生成器详情页面
 * @returns
 */
const GeneratorDetailPage: React.FC = () => {
  // 数据展示
  const [data, setData] = useState<API.GeneratorVO>({});
  // 加载状态
  const [loading, setLoading] = useState(true);
  const id = useParams().id;
  const loadData = async () => {
    if (!id) {
      return;
    }
    setLoading(true);
    try {
      const res = await getGeneratorVoByIdUsingGet({ id: Number(id) });
      if (res.data) {
        setData(res.data);
        setLoading(false);
      }
    } catch (error) {
      message.error('加载数据失败');
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 标签列表
  const tagListView = (tags?: string[]) => {
    if (!tags) {
      return <></>;
    }
    return (
      <div className="mb-4">
        {tags.map((tag: string) => {
          return <Tag key={tag}>{tag}</Tag>;
        })}
      </div>
    );
  };

  // tabs切换

  const onChange = (key: string) => {
    console.log(key);
  };

  const items: TabsProps['items'] = [
    {
      key: 'fileConfig',
      label: '文件配置',
      children: <FileConfig data={data} />,
    },
    {
      key: 'modelConfig',
      label: '模型配置',
      children: <ModelConfig data={data} />,
    },
    {
      key: 'authorInfo',
      label: '作者信息',
      children: <AuthorInfo data={data} />,
    },
  ];

  // 获取当前用户信息
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};

  // 编辑
  const editButton = (currentUser?.id === data.userId || currentUser?.userRole === 'admin') && (
    <Button
      icon={<EditOutlined />}
      onClick={() => {
        history.push(`/generator/add?id=${id}`);
      }}
    >
      编辑
    </Button>
  );

  // 下载
  const downloadButton = ((data.distPath !== null && data.distPath !== '') ||
    currentUser?.userRole === 'admin') && (
    <Button
      // 未登录，不允许下载
      disabled={!currentUser?.id}
      icon={<DownloadOutlined />}
      onClick={async () => {
        try {
          const blob = await downloadGeneratorByIdUsingGet(
            {
              id: Number(id),
            },
            {
              // 返回文件流
              responseType: 'blob',
            },
          );

          // 使用 file-saver 来保存文件
          const fullPath = COS_HOST + (data.distPath || '');
          // 获取文件名
          // 截取文件路径中最后一个 / 后面的字符串
          const fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
          saveAs(blob, fileName);
        } catch (error) {
          message.error('下载失败');
        }
      }}
    >
      下载
    </Button>
  );

  return (
    <PageContainer title={<></>} loading={loading}>
      {currentUser?.id ? (
        <></>
      ) : (
        <Alert message="未登录，不允许使用" type="warning" closable className="mb-4" />
      )}
      {data && (
        <Card>
          <Row justify="space-between" gutter={[32, 32]}>
            <Col flex="auto" span={12}>
              <Space size="large" align="center">
                <Typography.Title level={4}>{data.name}</Typography.Title>
                {tagListView(data.tags)}
              </Space>
              <Typography.Paragraph>{data.description}</Typography.Paragraph>
              <Typography.Paragraph>
                创建时间: {dayjs(data.createTime).format('YYYY-MM-DD HH:mm:ss')}
              </Typography.Paragraph>
              <Typography.Paragraph type="secondary">
                基础包: {data.basePackage}
              </Typography.Paragraph>
              <Typography.Paragraph type="secondary">版本: {data.version}</Typography.Paragraph>
              <Typography.Paragraph type="secondary">作者: {data.author}</Typography.Paragraph>

              <div className="mb-6"></div>

              <Space>
                <Link to={`/generator/use/${data.id}`}>
                  <Button type="primary" disabled={!currentUser?.id}>
                    立即使用
                  </Button>
                </Link>
                {/* 编辑 */}
                {editButton}
                {/* 下载 */}
                {downloadButton}
              </Space>
            </Col>
            <Col flex="320px" span={12}>
              <Image src={data?.picture} alt="" />
            </Col>
          </Row>
        </Card>
      )}

      <Card className="mt-12">
        <Tabs defaultActiveKey="fileConfig" items={items} onChange={onChange} />
      </Card>
    </PageContainer>
  );
};

export default GeneratorDetailPage;
