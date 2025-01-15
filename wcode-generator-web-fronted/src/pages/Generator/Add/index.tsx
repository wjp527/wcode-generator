import FileUploader from '@/components/FileUploader';
import PictureUploader from '@/components/PictureUploader';
import { COS_HOST } from '@/constants';
import {
  addGeneratorUsingPost,
  editGeneratorUsingPost,
  getGeneratorVoByIdUsingGet,
  toLeadUsingPost,
} from '@/services/backend/generatorController';
import type { ProFormInstance } from '@ant-design/pro-components';
import {
  ProCard,
  ProFormItem,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
  StepsForm,
} from '@ant-design/pro-components';
import { Button } from 'antd';
import { history, useSearchParams } from '@umijs/max';
import { Alert, message } from 'antd';
import { useEffect, useRef, useState } from 'react';
import ModelConfigForm from './components/ModelConfigForm';
import FilelConfigForm from './components/FilelConfigForm';
import GeneratorMaker from './components/GeneratorMaker';
import { FileAddOutlined, UploadOutlined } from '@ant-design/icons';
// 节流
import { throttle } from 'lodash';
/**
 * 生成器添加页面
 * @returns
 */
const GeneratorAddPage: React.FC = () => {
  const formRef = useRef<ProFormInstance>();
  const [searchParams] = useSearchParams();
  const id = searchParams.get('id');
  const [oldData, setOldData] = useState<API.GeneratorAddRequest>();
  // 基本信息
  const [basicInfo, setBasicInfo] = useState<API.GeneratorEditRequest>();
  // 模型配置
  const [modelConfig, setModelConfig] = useState<API.ModelConfig>();
  // 文件配置
  const [fileConfig, setFileConfig] = useState<API.FileConfig>();
  const [allData, setAllData] = useState<API.GeneratorAddRequest>();

  // 导入配置json文件
  const [fileList, setFileList] = useState<any[]>([]); // 用来存储子组件传递过来的文件列表

  /**
   * 加载数据
   */
  const loadData = async () => {
    if (!id) {
      return;
    }

    try {
      const res = await getGeneratorVoByIdUsingGet({ id: Number(id) });
      if (res.data) {
        const { distPath } = res.data ?? {};
        if (distPath) {
          // @ts-ignore
          res.data.distPath = [
            {
              uid: id,
              name: '文件' + id,
              status: 'done',
              url: COS_HOST + distPath,
              response: distPath,
            },
          ];
        }
        setOldData(res.data);
      }
    } catch (error) {
      message.error('获取数据失败');
    }
  };

  /**
   * 加载数据
   */
  useEffect(() => {
    if (!id) return;
    loadData();
  }, [id]);

  useEffect(() => {}, [oldData]);

  /**
   * 添加
   * @param values
   */
  const doAdd = async (values: API.GeneratorAddRequest) => {
    // 请求接口
    try {
      const res = await addGeneratorUsingPost(values);
      if (res.code === 0) {
        message.success('创建成功');
        history.push(`/generator/detail/${res.data}`);
      }
    } catch (error: any) {
      message.error('创建失败: ' + error.message);
    }
  };

  /**
   * 更新
   * @param values
   */
  const doUpdate = async (id: string, values: API.GeneratorEditRequest) => {
    // 请求接口
    try {
      const res = await editGeneratorUsingPost({
        ...values,
        id: Number(id),
      });
      if (res.code === 0) {
        message.success('更新成功');
        history.push(`/generator/detail/${id}`);
      }
    } catch (error: any) {
      message.error('更新失败: ', error.message);
    }
  };

  /**
   * 提交数据
   * @param values
   */
  const doSubmit = async (values: API.GeneratorAddRequest) => {
    // 数据转换
    if (!values.fileConfig) {
      values.fileConfig = {};
    }
    if (!values.modelConfig) {
      values.modelConfig = {};
    }
    if (values.distPath && values.distPath.length > 0) {
      // @ts-ignore
      values.distPath = values.distPath[0].response.data;
      setBasicInfo(values);
    }

    if (!id) {
      doAdd(values);
    } else {
      doUpdate(id, values);
    }
    localStorage.removeItem('basicInfo');
    localStorage.removeItem('modelConfig');
    localStorage.removeItem('fileConfig');
  };

  // 使用节流函数，确保每3秒钟只请求一次
  const throttledRequest = throttle(doSubmit, 10000);
  // 回显草稿数据
  useEffect(() => {
    if (!id) {
      // 从 LocalStorage 获取数据
      const savedBasicInfo = JSON.parse(localStorage.getItem('basicInfo') || '{}');
      const savedModelConfig = JSON.parse(localStorage.getItem('modelConfig') || '{}');
      const savedFileConfig = JSON.parse(localStorage.getItem('fileConfig') || '{}');
      // 更新表单值
      if (formRef.current) {
        formRef.current.setFieldsValue({
          ...savedBasicInfo, // 基本信息
          ...savedModelConfig, // 模型配置
          ...savedFileConfig, // 文件配置
        });
      }
    }
  }, []);

  // 保存草稿的主要函数
  const commandSave = async (type: string) => {
    // 本地 新建项目
    if (!id) {
      const formValues = await formRef.current?.validateFields();
      // 过滤掉undefined
      const newFormValues = Object.fromEntries(
        Object.entries(formValues).filter(([_, value]) => value !== undefined),
      );
      setBasicInfo(newFormValues);
      localStorage.setItem(type, JSON.stringify(newFormValues));
      message.success('保存成功');
    } else {
      // 使用异步更新状态并调用接口
      const formValues = formRef.current?.getFieldsValue();
      const newData = {
        ...allData,
        ...formValues,
      };

      setAllData(newData); // 更新状态

      try {
        const res = await editGeneratorUsingPost({ ...newData, id: Number(id) });
        if (res.code === 0) {
          message.success('保存成功');
        } else {
          message.error('保存失败');
        }
      } catch (error: any) {
        message.error('保存失败: ' + error.message);
      }
    }
  };
  // 基本信息保存
  const handleSave = async () => {
    commandSave('basicInfo');
  };

  // 保存模型配置
  const handleModelConfigSave = async () => {
    commandSave('modelConfig');
  };

  // 文件配置
  const handleFileConfigSave = async () => {
    commandSave('fileConfig');
  };

  // 子组件的 onChange 回调函数
  const handleFileChange = (newFileList: any[]) => {
    setFileList(newFileList); // 更新父组件的状态
  };

  // 导入模型配置json文件
  const [toLeadLoading, setToLeadLoading] = useState(false);
  const handleToLeadTemplateAsync = async (type: string) => {
    if (!type) {
      message.error('请选择要导入的文件');
      return;
    }
    if (fileList[0] !== undefined) {
      setToLeadLoading(true);
      const res = await toLeadUsingPost({
        key: fileList[0]?.response?.data || '',
        type,
      });
      if (res.code === 0) {
        message.success('导入成功');
        setOldData(res.data);
        setToLeadLoading(false);
      } else {
        message.error('导入失败');
        setToLeadLoading(false);
      }
    }
  };
  const handleToLeadTemplate = (type: string) => {
    handleToLeadTemplateAsync(type);
  };
  return (
    <>
      <ProCard>
        {(!id || oldData) && (
          <StepsForm<API.GeneratorAddRequest>
            formRef={formRef}
            // 表单初始值
            formProps={{
              initialValues: oldData,
            }}
            onFinish={throttledRequest}
          >
            <StepsForm.StepForm
              name="base"
              title="基本信息"
              onFinish={async (values) => {
                setBasicInfo(values);
                if (!id) {
                  // 动态回显下一步的数据
                  localStorage.setItem('basicInfo', JSON.stringify(values));
                  const nextStepData = JSON.parse(localStorage.getItem('modelConfig') || '{}');
                  setTimeout(() => {
                    if (formRef.current) {
                      formRef.current.setFieldsValue(nextStepData);
                    }
                  }, 0);
                } else {
                  const formValues = formRef.current?.getFieldsValue();
                  const newData = {
                    ...allData,
                    ...formValues,
                  };

                  setAllData(newData); // 更新状态
                }
                return true;
              }}
            >
              <ProFormText
                name="name"
                label="名称"
                placeholder="请输入名称"
                rules={[{ required: true }]}
              />
              <ProFormTextArea name="description" label="描述" placeholder="请输入描述" />
              <ProFormText name="basePackage" label="基础包" placeholder="请输入基础包" />
              <ProFormText name="version" label="版本" placeholder="请输入版本" />
              <ProFormText name="author" label="作用" placeholder="请输入作者" />
              <ProFormSelect name="tags" label="标签" mode="tags" placeholder="请输入标签" />
              <ProFormItem name="picture" label="图片">
                <PictureUploader biz="generator_picture" />
              </ProFormItem>
              <ProFormItem>
                <Button htmlType="button" onClick={handleSave}>
                  <FileAddOutlined />
                  保存为草稿
                </Button>
              </ProFormItem>
            </StepsForm.StepForm>
            <StepsForm.StepForm
              name="modelConfig"
              title="模型配置"
              onFinish={async (values) => {
                setModelConfig(values);
                // 动态回显下一步的数据
                if (!id) {
                  localStorage.setItem('modelConfig', JSON.stringify(values));
                  const nextStepData = JSON.parse(localStorage.getItem('fileConfig') || '{}');
                  setTimeout(() => {
                    if (formRef.current) {
                      formRef.current.setFieldsValue(nextStepData); // 更新表单值
                    }
                  }, 0);
                } else {
                  const formValues = formRef.current?.getFieldsValue();
                  const newData = {
                    ...allData,
                    ...formValues,
                  };

                  setAllData(newData); // 更新状态
                }
                return true;
              }}
            >
              <ProFormItem>
                <FileUploader
                  biz="generator_to_lead_by_model_template"
                  description="请上传模型配置json文件"
                  value={fileList} // 将父组件的 fileList 传给子组件
                  onChange={handleFileChange} // 传递回调函数给子组件
                />

                <div className="mt-10 flex">
                  <div className="mr-4">
                    <Button htmlType="button" onClick={handleModelConfigSave}>
                      <FileAddOutlined />
                      保存为草稿
                    </Button>
                  </div>
                  <div>
                    <Button
                      htmlType="button"
                      type="primary"
                      onClick={() => handleToLeadTemplate('generator_to_lead_by_model_template')}
                      loading={toLeadLoading}
                    >
                      <UploadOutlined />
                      导入
                    </Button>
                  </div>
                </div>
              </ProFormItem>
              <ModelConfigForm formRef={formRef} oldData={oldData} />
            </StepsForm.StepForm>
            <StepsForm.StepForm
              name="fileConfig"
              title="文件配置"
              onFinish={async (values) => {
                setFileConfig(values);
                if (!id) {
                  localStorage.setItem('fileConfig', JSON.stringify(values));
                }
                return true;
              }}
            >
              <Alert message="如果不需要使用功能在线制作功能，可不填写" type="warning" closable />
              <div className="m-10"></div>
              <ProFormItem>
                <FileUploader
                  biz="generator_to_lead_by_file_template"
                  description="请上传文件配置json文件"
                  value={fileList} // 将父组件的 fileList 传给子组件
                  onChange={handleFileChange} // 传递回调函数给子组件
                />

                <div className="mt-10 flex">
                  <div className="mr-4">
                    <Button htmlType="button" onClick={handleFileConfigSave}>
                      <FileAddOutlined />
                      保存为草稿
                    </Button>
                  </div>
                  <div>
                    <Button
                      htmlType="button"
                      type="primary"
                      onClick={() => handleToLeadTemplate('generator_to_lead_by_file_template')}
                      loading={toLeadLoading}
                    >
                      <UploadOutlined />
                      导入
                    </Button>
                  </div>
                </div>
              </ProFormItem>
              <FilelConfigForm formRef={formRef} oldData={oldData} />
            </StepsForm.StepForm>
            <StepsForm.StepForm name="dist" title="生成器文件">
              <ProFormItem name="distPath" label="产物包">
                {/*
                  文件上传组件
                */}
                <FileUploader biz="generator_dist" description="请上传生成器压缩包" />
              </ProFormItem>
              {/* <ProFormItem name="meta" label="元数据"> */}
              <GeneratorMaker
                meta={{
                  ...basicInfo,
                  ...modelConfig,
                  ...fileConfig,
                }}
              />
              {/* </ProFormItem> */}
            </StepsForm.StepForm>
          </StepsForm>
        )}
      </ProCard>
    </>
  );
};

export default GeneratorAddPage;
