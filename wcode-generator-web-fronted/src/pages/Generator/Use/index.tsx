import { useEffect, useState } from 'react';
import {
  getGeneratorVoByIdUsingGet,
  useGeneratorUsingPost,
} from '@/services/backend/generatorController';
import { DownloadOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { useParams, useModel, Link } from '@umijs/max';
import {
  Card,
  Col,
  Image,
  message,
  Row,
  Space,
  Tag,
  Typography,
  Button,
  Form,
  Input,
  Select,
} from 'antd';
import { Collapse } from 'antd';
import '@/index.css';
import { COS_HOST } from '@/constants';
import { saveAs } from 'file-saver';
/**
 * 使用器详情页面
 * @returns
 */
const GeneratorUsePage: React.FC = () => {
  // 数据展示
  const [data, setData] = useState<API.GeneratorVO>({});
  // 加载状态
  const [loading, setLoading] = useState(true);
  const [downloadLoading, setDownloadLoading] = useState(false);

  const models = data?.modelConfig?.models || [];
  const [form] = Form.useForm();

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

  // 获取当前用户信息
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};

  // 下载
  const handleDownload = async () => {
    const dataModel = form.getFieldsValue();
    setDownloadLoading(true);
    try {
      /* eslint-disable react-hooks/rules-of-hooks */
      const blob = await useGeneratorUsingPost(
        {
          id: Number(id),
          dataModel: dataModel,
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
    setDownloadLoading(false);
  };

  const downloadButton = ((data.distPath !== null && data.distPath !== '') ||
    currentUser?.userRole === 'admin') && (
    <Button
      type="primary"
      disabled={downloadLoading}
      icon={<DownloadOutlined />}
      onClick={handleDownload}
      loading={downloadLoading}
    >
      生成代码
    </Button>
  );

  // 立使使用
  const useCode = () => {
    return (
      <Form form={form} className="mb-10">
        {models.map((model) => {
          // 如果是分组类型模型
          if (model.groupKey) {
            if (!model.models) {
              return <></>;
            }
            return (
              <Collapse
                key={model.groupKey}
                className="mb-10"
              >
                <Collapse.Panel
                  key={model.groupKey}
                  header={model.description}
                >
                  {model?.models?.map((subModel) => (
                    <Form.Item
                      label={subModel.fieldName}
                      // @ts-ignore
                      name={[model.groupKey, subModel.fieldName]}
                      key={subModel.fieldName}
                    >
                      <Input
                        placeholder={subModel.description}
                        defaultValue={subModel?.defaultValue + ''}
                      />
                    </Form.Item>
                  ))}
                </Collapse.Panel>
              </Collapse>
            );
          } else {
            // 不是分组的
            return (
              <Form.Item label={model.fieldName} name={model.fieldName} key={model.fieldName}>
                {model.type === 'boolean' ? (
                  <Select defaultValue={model.defaultValue + ''}>
                    <Select.Option value="true">true</Select.Option>
                    <Select.Option value="false">false</Select.Option>
                  </Select>
                ) : (
                  <Input placeholder={model.description} />
                )}
              </Form.Item>
            );
          }
        })}
      </Form>
    );
  };

  return (
    <PageContainer title={<></>} loading={loading}>
      {data && (
        <Card>
          <Row justify="space-between" gutter={[32, 32]}>
            <Col flex="auto" span={12}>
              <Space size="large" align="center">
                <Typography.Title level={4}>{data.name}</Typography.Title>
                {tagListView(data.tags)}
              </Space>
              <Typography.Paragraph>{data.description}</Typography.Paragraph>

              <div className="mb-6"></div>
              {useCode()}

              <Space size="middle">
                {downloadButton}
                <Link to={`/generator/detail/${id}`}>
                  <Button>查看详情</Button>
                </Link>
              </Space>
            </Col>
            <Col flex="320px" span={12}>
              <Image src={data?.picture} alt="" />
            </Col>
          </Row>
        </Card>
      )}
    </PageContainer>
  );
};

export default GeneratorUsePage;
