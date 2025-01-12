import { Avatar, Card } from 'antd';

import '@/index.css';
/**
 * 生成器详情页面
 * @returns
 */

interface Props {
  data: API.GeneratorVO;
}

/**
 * 作者信息
 * @param props
 * @returns
 */
const AuthorInfo: React.FC<Props> = (props) => {
  const { data } = props;
  if (!data) {
    return <></>;
  }
  return (
    <div className="mt-4">
      <Card.Meta
        title={data.user?.userName}
        description={data.description}
        avatar={<Avatar src={data?.user?.userAvatar} size={64} />}
      ></Card.Meta>
    </div>
  );
};

export default AuthorInfo;
