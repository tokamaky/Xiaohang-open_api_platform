import { listInterfaceInfoVoByUserIdPageUsingPost} from '@/services/xiaohang-backend/interfaceInfoController';
import { ShareAltOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { Card, Layout, List, message, Pagination, PaginationProps, Tooltip } from 'antd';
import Search from 'antd/es/input/Search';
import { Content, Footer, Header } from 'antd/es/layout/layout';
import React, { useEffect, useState } from 'react';
import indexStyle from './index.less';

const headerStyle: React.CSSProperties = {
    textAlign: 'center',
    height: '64px',
    paddingInline: '30%',
    lineHeight: '64px',
    color: '#fff',
    background: 'none',
};
const footerStyle: React.CSSProperties = {
    textAlign: 'center',
};

const contentStyle: React.CSSProperties = {
    minHeight: 120,
    lineHeight: '120px',
};

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
                console.log(res.data);
                setTotal(res?.data?.total ?? 0);
                setList(res?.data?.records ?? []);
            });
        } catch (error: any) {
            message.error('Request Failed，' + error.message);
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
        console.log(pageNumber);
        setCurrent(pageNumber);
        loadData(searchText, pageNumber);
    };

    const onSizeChange = (current: number, size: number) => {
        loadData(searchText, current, size);
    };
// Function to handle sharing
  const handleShare = (item: API.InterfaceInfoVO) => {
    if (navigator.share) {
      navigator.share({
        title: item.name,
        text: item.description || 'No description available.',
        url: window.location.href + '/interface_info/' + item.id, // Share the link to the interface details page
      })
        .then(() => message.success('Successfully shared!'))
        .catch((error) => message.error('Error sharing the page'));
    } else {
      // Fallback for browsers that don't support the Web Share API
      message.info('Share URL copied to clipboard!');
      navigator.clipboard.writeText(window.location.href + '/interface_info/' + item.id); // Copy link to clipboard
    }
  };

    const CardInfo: React.FC<{
        totalNum: React.ReactNode;
        leftNum: React.ReactNode;
    }> = ({ totalNum, leftNum }) => (
        <div className={indexStyle.cardInfo}>
            <div>
                <p>Number of Invocations</p>
                <p>{totalNum}</p>
            </div>
            <div>
                <p>Remaining Number of Invocations</p>
                <p>{leftNum}</p>
            </div>
        </div>
    );

    return (
        <PageContainer>
            <Layout>
                <Header style={headerStyle}>
                    <Search
                        size={'large'}
                        placeholder="Please Enter the Interface Name"
                        onSearch={onSearch}
                        enterButton
                    />
                </Header>
                <Content style={contentStyle}>
                    <List<API.InterfaceInfoVO>
                        className={indexStyle.filterCardList}
                        grid={{ gutter: 24, xxl: 3, xl: 2, lg: 2, md: 2, sm: 2, xs: 1 }}
                        dataSource={list || []}
                        loading={loading}
                        renderItem={(item) => (
                            <List.Item>
                                <Card
                                    hoverable
                                    bodyStyle={{ paddingBottom: 20 }}
                                    actions={[
                                        <Tooltip title="Share" key="share">
                                          <ShareAltOutlined onClick={() => handleShare(item)} />
                                        </Tooltip>,
                                        <Tooltip title="Online Invocation" key="onlineInvocation">
                                            <div
                                                onClick={() => {
                                                    history.push('/interface_info/' + item.id);
                                                }}
                                            >
                                                Online Invocation
                                            </div>
                                        </Tooltip>,
                                    ]}
                                >
                                    <Card.Meta title={item.name} />
                                    <div>
                                        <CardInfo totalNum={item.totalNum} leftNum={item.leftNum} />
                                    </div>
                                </Card>
                            </List.Item>
                        )}
                    />
                    <Footer style={footerStyle}>
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
                </Content>
            </Layout>
        </PageContainer>
    );
};

export default Index;
