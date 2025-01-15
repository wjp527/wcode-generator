import CreateModal from '@/pages/Admin/Generator/components/CreateModal';
import UpdateModal from '@/pages/Admin/Generator/components/UpdateModal';
import {
  deleteGeneratorUsingPost,
  listGeneratorByPageUsingPost,
} from '@/services/backend/generatorController';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Button, message, Modal, Select, Space, Tag, Tooltip, Typography } from 'antd';
import React, { useRef, useState } from 'react';

// 默认分页参数
const DEFAULT_PAGE_PARAMS = {
  current: 1,
  pageSize: 12,
  searchText: '',
  sortField: 'createTime',
  sortOrder: 'descend',
};

/**
 * 代码生成器管理页面
 *
 * @constructor
 */
const GeneratorAdminPage: React.FC = () => {
  // 是否显示新建窗口
  const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);
  // 是否显示更新窗口
  const [updateModalVisible, setUpdateModalVisible] = useState<boolean>(false);
  const actionRef = useRef<ActionType>();
  // 当前代码生成器点击的数据
  const [currentRow, setCurrentRow] = useState<API.Generator>();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [type, setType] = useState<'file' | 'model'>('file');
  const [selectConfig, setSelectConfig] = useState<API.Generator>();
  const showModal = () => {
    setIsModalOpen(true);
  };
  const handleOk = () => {
    setIsModalOpen(false);
  };
  const handleCancel = () => {
    setIsModalOpen(false);
  };
  // 查看文件配置
  const lookConfig = (record: API.Generator, type: 'file' | 'model') => {
    showModal();
    setType(type);
    if (type === 'file') {
      setSelectConfig(JSON.parse(record.fileConfig || '{}'));
    } else {
      setSelectConfig(JSON.parse(record.modelConfig || '{}'));
    }
  };

  /**
   * 删除节点
   *
   * @param row
   */
  const handleDelete = async (row: API.Generator) => {
    const hide = message.loading('正在删除');
    if (!row) return true;
    try {
      await deleteGeneratorUsingPost({
        id: row.id as any,
      });
      hide();
      message.success('删除成功');
      actionRef?.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error('删除失败，' + error.message);
      return false;
    }
  };

  /**
   * 表格列配置
   */
  const columns: ProColumns<API.Generator>[] = [
    {
      title: 'id',
      dataIndex: 'id',
      valueType: 'text',
      hideInForm: true,
    },
    {
      title: '名称',
      dataIndex: 'name',
      valueType: 'text',
    },
    {
      title: '头像',
      dataIndex: 'picture',
      valueType: 'image',
      fieldProps: {
        width: 64,
      },
      hideInSearch: true,
    },
    {
      title: '简介',
      dataIndex: 'description',
      valueType: 'textarea',
    },
    {
      title: '基础包',
      dataIndex: 'basePackage',
      valueType: 'text',
    },
    {
      title: '账号',
      dataIndex: 'author',
      valueType: 'text',
    },
    {
      title: '版本',
      dataIndex: 'version',
      valueType: 'text',
    },
    {
      title: '标签',
      dataIndex: 'tags',
      valueType: 'text',
      /**
       *  renderFormItem 函数的作用是，当用户在表格的“标签”列上编辑时，
       * 会展示一个标签输入框（即 Select 组件，mode="tags"），用户可以选择或输入标签。
       */
      renderFormItem: (schema) => {
        const { fieldProps } = schema;
        // @ts-ignore
        return <Select mode="tags" {...fieldProps} />;
      },
      render: (_, record) => {
        if (!record.tags) {
          return <></>;
        }
        // 转为tag标签
        return JSON.parse(record.tags).map((tag: string) => {
          return <Tag key={tag}>{tag}</Tag>;
        });
      },
    },
    {
      title: '文件配置',
      dataIndex: 'fileConfig',
      render: (_, record) => {
        // 转为tag标签
        return (
          <Button size="small" onClick={() => lookConfig(record, 'file')}>
            文件配置
          </Button>
        );
      },
    },
    {
      title: '模型配置',
      dataIndex: 'modelConfig',
      render: (_, record) => {
        // 转为tag标签
        return (
          <Button size="small" onClick={() => lookConfig(record, 'model')}>
            模型配置
          </Button>
        );
      },
    },
    {
      title: '产物包路径',
      dataIndex: 'distPath',
      valueType: 'text',
      // 超过10个字符就显示省略号
      render: (_, record: any) => {
        return (
          <Tooltip title={record.distPath}>
            {record.distPath?.length > 10 ? record.distPath?.slice(0, 10) + '...' : record.distPath}
          </Tooltip>
        );
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueEnum: {
        0: {
          text: '未开始',
        },
        1: {
          text: '制作中',
        },
        2: {
          text: '打包中',
        },
        3: {
          text: '审核中',
        },
        4: {
          text: '已发布',
        },
        5: {
          text: '已下架',
        },
      },
    },
    {
      title: '创建用户',
      dataIndex: 'userId',
      valueType: 'text',
      render: (_, record: any) => {
        return (
          <Tooltip title={record.userId}>
            {record.userId?.length > 10 ? record.userId?.slice(0, 10) + '...' : record.userId}
          </Tooltip>
        );
      },
    },
    {
      title: '创建时间',
      sorter: true,
      dataIndex: 'createTime',
      valueType: 'dateTime',
      hideInSearch: true,
      hideInForm: true,
    },
    {
      title: '更新时间',
      sorter: true,
      dataIndex: 'updateTime',
      valueType: 'dateTime',
      hideInSearch: true,
      hideInForm: true,
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, record) => (
        <Space size="middle">
          <Typography.Link
            onClick={() => {
              setCurrentRow(record);
              setUpdateModalVisible(true);
            }}
          >
            修改
          </Typography.Link>
          <Typography.Link type="danger" onClick={() => handleDelete(record)}>
            删除
          </Typography.Link>
        </Space>
      ),
    },
  ];
  return (
    // PageContainer: 会生成页面容器【不过这个页面容器会定死】，包含标题、面包屑、工具栏等
    <div>
      <Typography.Title level={2}>代码生成器管理</Typography.Title>
      <ProTable<API.Generator>
        headerTitle={'查询表格'}
        actionRef={actionRef}
        rowKey="key"
        search={{
          labelWidth: 120,
        }}
        toolBarRender={() => [
          <Button
            type="primary"
            key="primary"
            onClick={() => {
              setCreateModalVisible(true);
            }}
          >
            <PlusOutlined /> 新建
          </Button>,
        ]}
        request={async (params, sort, filter) => {
          const sortField = Object.keys(sort)?.[0];
          const sortOrder = sort?.[sortField] ?? undefined;

          const { data, code } = await listGeneratorByPageUsingPost({
            ...params,
            sortField,
            sortOrder,
            ...filter,
          } as API.GeneratorQueryRequest);

          return {
            success: code === 0,
            data: data?.records || [],
            total: Number(data?.total) || 0,
          };
        }}
        pagination={{
          pageSize: DEFAULT_PAGE_PARAMS.pageSize,
        }}
        columns={columns}
      />
      <CreateModal
        visible={createModalVisible}
        columns={columns}
        onSubmit={() => {
          setCreateModalVisible(false);
          actionRef.current?.reload();
        }}
        onCancel={() => {
          setCreateModalVisible(false);
        }}
      />
      <UpdateModal
        visible={updateModalVisible}
        columns={columns}
        oldData={currentRow}
        onSubmit={() => {
          setUpdateModalVisible(false);
          setCurrentRow(undefined);
          actionRef.current?.reload();
        }}
        onCancel={() => {
          setUpdateModalVisible(false);
        }}
      />

      <Modal
        title={type === 'file' ? '文件配置' : '模型配置'}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={handleCancel}
      >
        <pre>{JSON.stringify(selectConfig, null, 2)}</pre>
      </Modal>
    </div>
  );
};
export default GeneratorAdminPage;
