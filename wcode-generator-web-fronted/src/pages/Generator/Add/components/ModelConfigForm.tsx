import React, { useEffect } from 'react';
import { CloseOutlined } from '@ant-design/icons';
import { Button, Card, Form, FormListFieldData, Input, Space } from 'antd';

/**
 * 模型配置表单
 */
interface ModelConfigFormProps {
  formRef: any;
  oldData: any;
}

/**
 *  <Form.List name={['modelConfig', 'models']}>
      <Form.Item label="组内字段" name={[field.name, 'models']}>
         <Form.List name={[field.name, 'models']}>

         </Form.List>
      </Form.Item>
    </Form.List>
  中的 <Form.Item label="组内字段" name={[field.name, 'models']}>
  name是不是可以这么理解:
  因为在最外层 name={['modelConfig', 'models']} 已经开始遍历，
  那么<Form.Item label="组内字段" name={[field.name, 'models']}>
  中的 field.name 就是他遍历下的某一个对象的索引，而这个models子对象，就是这个某一个对象的索引中的对象
 */

const ModelConfigForm: React.FC<ModelConfigFormProps> = ({ formRef, oldData }) => {
  useEffect(() => {
    // 当 oldData 更新时，通过 setFieldsValue 更新表单的值
    if (oldData) {
      formRef.current?.setFieldsValue({
        modelConfig: oldData?.modelConfig, // 确保 modelConfig 数据结构正确
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
      <Space key={field.key}>
        <Form.Item label="字段名称" name={[field.name, 'fieldName']}>
          <Input />
        </Form.Item>
        <Form.Item label="描述" name={[field.name, 'description']}>
          <Input />
        </Form.Item>
        <Form.Item label="类型" name={[field.name, 'type']}>
          <Input />
        </Form.Item>
        <Form.Item label="默认值" name={[field.name, 'defaultValue']}>
          <Input />
        </Form.Item>
        <Form.Item label="缩写" name={[field.name, 'abbr']}>
          <Input />
        </Form.Item>
        {remove && (
          <Button type="text" danger onClick={() => remove(field.name)}>
            删除
          </Button>
        )}
      </Space>
    );
  };

  return (
    // 这里之所以没有对表单进行赋初值，因为在外层的时候已经做过了
    // ['modelConfig', 'models']: 表示每个字段都在 modelConfig.modeles 中
    <>
      <Form.List name={['modelConfig', 'models']}>
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
                const modelConfig =
                  formRef?.current?.getFieldsValue()?.modelConfig ?? oldData?.modelConfig;
                // 获取获取分组key
                const groupKey = modelConfig?.models?.[field.name]?.groupKey;
                return (
                  <Card
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
                        <Form.Item label="类型" name={[field.name, 'type']}>
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
                      <Form.Item label="组内字段" name={[field.name, 'models']}>
                        <Form.List name={[field.name, 'models']}>
                          {/* subFields.map() 遍历内层的 models 数组，每次遍历都会渲染一个表单项。 */}
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
                    groupKey: 'group',
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
    </>
  );
};
// 效果:
// "modelConfig": {
//   "models": [
//     {
//       "fieldName": "needGit",
//       "type": "boolean",
//       "description": "是否生成 .gitignore 文件",
//       "defaultValue": true,
//       "abbr":  "n"
//     },
//     {
//       "fieldName": "loop",
//       "type": "boolean",
//       "description": "是否生成循环",
//       "defaultValue": false,
//       "abbr": "l"
//     },
//     {
//       "groupKey": "mainTemplate",
//       "groupName": "核心模块",
//       "type": "MainTemplate",
//       "description": "用于生成核心模块文件",
//       "condition": "loop",
//       "models": [
//         {
//           "fieldName": "author",
//           "type": "String",
//           "description": "作者注释",
//           "defaultValue": "wjp",
//           "abbr": "a"
//         },
//         {
//           "fieldName": "outputText",
//           "type": "String",
//           "description": "输出信息",
//           "defaultValue": "sum = ",
//           "abbr": "o"
//         }
//       ]
//     }
//   ]
// }
export default ModelConfigForm;
