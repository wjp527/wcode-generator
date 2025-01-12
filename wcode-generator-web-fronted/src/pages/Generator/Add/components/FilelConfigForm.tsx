import React, { useEffect } from 'react';
import { CloseOutlined } from '@ant-design/icons';
import { Button, Card, Form, FormListFieldData, Input, Select, Space } from 'antd';
import '@/index.css';
/**
 * 模型配置表单
 */
interface FilelConfigFormProps {
  formRef: any;
  oldData: any;
}

/**
 *  <Form.List name={['fileConfig', 'files']}>
      <Form.Item label="组内字段" name={[field.name, 'files']}>
         <Form.List name={[field.name, 'files']}>

         </Form.List>
      </Form.Item>
    </Form.List>
  中的 <Form.Item label="组内字段" name={[field.name, 'files']}>
  name是不是可以这么理解:
  因为在最外层 name={['fileConfig', 'files']} 已经开始遍历，
  那么<Form.Item label="组内字段" name={[field.name, 'files']}>
  中的 field.name 就是他遍历下的某一个对象的索引，而这个files子对象，就是这个某一个对象的索引中的对象
 */

const FilelConfigForm: React.FC<FilelConfigFormProps> = ({ formRef, oldData }) => {
  console.log(formRef, 'formRef');

  useEffect(() => {
    // 当 oldData 更新时，通过 setFieldsValue 更新表单的值
    if (oldData) {
      formRef.current?.setFieldsValue({
        fileConfig: oldData?.fileConfig, // 确保 modelConfig 数据结构正确
      });
    }
  }, [oldData]); // 依赖 oldData，确保数据变化时更新表单

  /**
   * 单个字段表单视图
   * @param field 字段
   * @param remove 删除函数
   * @returns
   */
  const singleFieldFormView = (
    field: FormListFieldData,
    remove?: (index: number | number[]) => void,
  ) => {
    return (
      <>
        <Space key={field.key}>
          <Form.Item label="输入路径" name={[field.name, 'inputPath']}>
            <Input />
          </Form.Item>
          <Form.Item label="输出路径" name={[field.name, 'outputPath']}>
            <Input />
          </Form.Item>
          <Form.Item label="类型" name={[field.name, 'type']}>
            <Select
              className="min-w-20"
              options={[
                {
                  value: 'file',
                  label: '文件',
                },
                {
                  value: 'dir',
                  label: '目录',
                },
              ]}
            ></Select>
          </Form.Item>
          <Form.Item label="生成类型" name={[field.name, 'generateType']}>
            <Select
              className="min-w-20"
              options={[
                {
                  value: 'static',
                  label: '静态',
                },
                {
                  value: 'dynamic',
                  label: '动态',
                },
              ]}
            ></Select>
          </Form.Item>
          <Form.Item label="条件 " name={[field.name, 'condition']}>
            <Input />
          </Form.Item>
          {remove && (
            <Button type="text" danger onClick={() => remove(field.name)}>
              删除
            </Button>
          )}
        </Space>
      </>
    );
  };

  return (
    // 这里之所以没有对表单进行赋初值，因为在外层的时候已经做过了
    // ['fileConfig', 'files']: 表示每个字段都在 fileConfig.filees 中
    <Form.List name={['fileConfig', 'files']}>
      {/*
        {
          fieldKey: 0,       // 唯一标识符，用于 React 重新渲染，确保每个字段的状态保持一致
          isListField: true, // 这是一个列表字段
          key: 0,            // React 的 key，确保列表项的唯一性
          name: 0            // 表示这是 Form.List 中的第一项，name 是动态生成的索引
        }
      */}

      {(fields, { add, remove }) => {
        return (
          <div style={{ display: 'flex', rowGap: 16, flexDirection: 'column' }}>
            {fields.map((field) => {
              // 获取表单数据
              const fileConfig =
                formRef?.current?.getFieldsValue()?.fileConfig ?? oldData?.fileConfig;
              // 获取获取分组key
              const groupKey = fileConfig?.files?.[field.name]?.groupKey;
              return (
                <Card
                  className="mb-10"
                  size="small"
                  title={groupKey ? '分组' : '未分组字段'}
                  key={field.key}
                  extra={
                    <CloseOutlined
                      onClick={() => {
                        remove(field.name);
                      }}
                    />
                  }
                >
                  {groupKey ? (
                    <Space>
                      {/*
                        <Form.Item label="分组key" name={[field.name, 'groupKey']}>
                        <Form.Item label="组名" name={[field.name, 'groupName']}>
                        不理解既然field.name都是唯一的了，
                        groupKey的作用: 用来收集数据，最终传给后端
                      */}
                      <Form.Item label="分组key" name={[field.name, 'groupKey']}>
                        <Input />
                      </Form.Item>
                      <Form.Item label="组名" name={[field.name, 'groupName']}>
                        <Input />
                      </Form.Item>
                      <Form.Item label="条件" name={[field.name, 'condition']}>
                        <Input />
                      </Form.Item>
                    </Space>
                  ) : (
                    singleFieldFormView(field)
                  )}

                  {/* 组内字段 */}
                  {groupKey && (
                    <Form.Item label="组内字段" name={[field.name, 'files']}>
                      <Form.List name={[field.name, 'files']}>
                        {/* subFields.map() 遍历内层的 files 数组，每次遍历都会渲染一个表单项。 */}
                        {(subFields, subOpt) => (
                          <div className="flex flex-col gap-4">
                            {subFields.map((subField) =>
                              // subOpt: 包含 add、remove方法
                              // singleFieldFormView(subField, subOpt.remove) 渲染每个表单项，并提供删除功能。
                              singleFieldFormView(subField, subOpt.remove),
                            )}
                            <Button type="dashed" onClick={() => subOpt.add()} block>
                              添加组内字段
                            </Button>
                          </div>
                        )}
                      </Form.List>
                    </Form.Item>
                  )}
                </Card>
              );
            })}

            <Button type="dashed" onClick={() => add()}>
              添加字段
            </Button>
            <Button
              type="dashed"
              onClick={() =>
                add({
                  groupName: '分组',
                  groupKey: 'groupKey',
                  type: 'group',
                })
              }
            >
              添加分组
            </Button>

            <div className="mb-4"></div>
          </div>
        );
      }}
    </Form.List>
  );
};
export default FilelConfigForm;
