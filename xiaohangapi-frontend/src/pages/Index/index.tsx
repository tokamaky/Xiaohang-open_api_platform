import { PageContainer } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import {Card, List, message, theme} from 'antd';
import React, {useEffect, useState} from 'react';
import {listInterfaceInfoByPageUsingGet} from "@/services/xiaohangapi-backend/interfaceInfoController";


/**
 * 主页
 * @constructor
 */
const Index: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [list, setList] = useState<API.InterfaceInfo[]>([]);
  const [total, setTotal] = useState<number>(0);

  const loadData = async (current = 1, pageSize = 10) => {
    setLoading(true);
    try {
      const res = await listInterfaceInfoByPageUsingGet({
        current,
        pageSize,
      });
      setList(res?.data?.records ?? []);
      setTotal(res?.data?.total ?? 0);
    } catch (error: any) {
      message.error('Operation failed，' + error.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  return (
    <PageContainer title={'主页'}>
      <List
        className="interfaceInfo-list"
        loading={loading}
        itemLayout="horizontal"
        dataSource={list}
        pagination={{
          showSizeChanger: true,
          total: total,
          showTotal(total, range) {
            return `${range[0]}-${range[1]} / ${total}`;
          },
          onChange(page, pageSize) {
            loadData(page, pageSize);
          },
        }}

        renderItem={(item) => {
          const apiLink = `/interface_info/${item.id}`;
          return (
            <List.Item
              actions={[
                <a key={item.id} href={apiLink}>
                  Check
                </a>,
              ]}
            >
              <List.Item.Meta
                title={<a href={apiLink}>{item.name}</a>}
                description={item.description}
              />
              <div>{item.method}</div>
            </List.Item>
          );
        }}
      />
    </PageContainer>
  );
};

export default Index;
