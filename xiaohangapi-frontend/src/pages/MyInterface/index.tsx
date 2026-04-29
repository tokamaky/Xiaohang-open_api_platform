import { listInterfaceInfoVoByUserIdPageUsingPost } from '@/services/xiaohang-backend/interfaceInfoController';
import { ShareAltOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { Card, Layout, List, message, Pagination, PaginationProps, Tooltip } from 'antd';
import Search from 'antd/es/input/Search';
import { Content, Footer } from 'antd/es/layout/layout';
import React, { useEffect, useState } from 'react';
import indexStyle from './index.less';
import './index.less';

const Index: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [total, setTotal] = useState<number>(0);
  const [current, setCurrent] = useState<number>(1);
  const [list, setList] = useState<API.InterfaceInfoVO[]>([]);

  const loadData = async (searchText = '', current = 1, pageSize = 6) => {
    setLoading(true);
    try {
      await listInterfaceInfoVoByUserIdPageUsingPost({
        name: searchText,
        current,
        pageSize,
      }).then((res) => {
        setTotal(res?.data?.total ?? 0);
        setList(res?.data?.records ?? []);
      });
    } catch (error: any) {
      message.error('Request Failed: ' + error.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  const onSearch = (value: string) => {
    setSearchText(value);
    loadData(value);
  };

  const onChange: PaginationProps['onChange'] = (pageNumber) => {
    setCurrent(pageNumber);
    loadData(searchText, pageNumber);
  };

  const onSizeChange = (page: number, size: number) => {
    loadData(searchText, page, size);
  };

  const handleShare = (item: API.InterfaceInfoVO) => {
    const url = window.location.origin + `/interface_info/${item.id}`;
    if (navigator.share) {
      navigator.share({ title: item.name, text: item.description || '', url })
        .then(() => message.success('Shared successfully!'))
        .catch(() => message.info('URL copied to clipboard!'));
    } else {
      navigator.clipboard.writeText(url);
      message.info('URL copied to clipboard!');
    }
  };

  return (
    <PageContainer>
      <Layout className="myinterface-layout">
        <div className="myinterface-search">
          <Search
            size="large"
            placeholder="Filter interfaces by name..."
            onSearch={onSearch}
            enterButton={<span className="search-btn-inner">Search</span>}
          />
        </div>

        <Content className="myinterface-content">
          <List<API.InterfaceInfoVO>
            className={indexStyle.filterCardList}
            grid={{ gutter: 24, xxl: 3, xl: 2, lg: 2, md: 2, sm: 2, xs: 1 }}
            dataSource={list || []}
            loading={loading}
            renderItem={(item) => (
              <List.Item>
                <Card
                  className="interface-card"
                  hoverable
                  bodyStyle={{ padding: 0 }}
                  actions={[
                    <Tooltip title="Share" key="share">
                      <ShareAltOutlined onClick={() => handleShare(item)} />
                    </Tooltip>,
                    <Tooltip title="Online Invocation" key="invoke">
                      <div
                        className="card-action-link"
                        onClick={() => history.push('/interface_info/' + item.id)}
                      >
                        Invoke
                      </div>
                    </Tooltip>,
                  ]}
                >
                  <div className="card-inner">
                    <div className="card-header-row">
                      <span className={`method-badge method-${(item.method || 'get').toLowerCase()}`}>
                        {item.method?.toUpperCase() || 'GET'}
                      </span>
                      <span className={`status-dot ${item.status === 1 ? 'status-online' : 'status-offline'}`} />
                    </div>
                    <h3 className="card-title">{item.name}</h3>
                    <p className="card-desc">{item.description || 'No description provided.'}</p>
                    <div className="card-stats">
                      <div className="stat-item">
                        <span className="stat-label">Invocations</span>
                        <span className="stat-value">{item.totalNum ?? 0}</span>
                      </div>
                      <div className="stat-divider" />
                      <div className="stat-item">
                        <span className="stat-label">Remaining</span>
                        <span className="stat-value stat-remaining">{item.leftNum ?? 0}</span>
                      </div>
                    </div>
                  </div>
                </Card>
              </List.Item>
            )}
          />
        </Content>

        <Footer className="myinterface-footer">
          <Pagination
            showQuickJumper
            showSizeChanger
            pageSizeOptions={[6, 10, 20, 30]}
            current={current}
            onShowSizeChange={onSizeChange}
            defaultPageSize={6}
            total={total}
            onChange={onChange}
          />
        </Footer>
      </Layout>
    </PageContainer>
  );
};

export default Index;
