import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { Layout, Typography, Card, List, Tag, Button } from 'antd';
import { DatabaseOutlined,  FileOutlined, DeploymentUnitOutlined, ExclamationCircleOutlined, WarningOutlined, CheckCircleOutlined, CloudOutlined } from '@ant-design/icons';

const { Content } = Layout;
const { Title, Paragraph } = Typography;

const Todo: React.FC = () => {
  // Sample tasks with status, importance level, and icons
  const tasks = [
    {
      section: 'Database',
      icon: <DatabaseOutlined style={{ color: '#1890ff', fontSize: '20px' }} />,
      items: [
        { description: 'Add Redis data structure server', importance: 'high' }, // high priority
      ],
    },
    {
      section: 'Backend',
      icon: <CloudOutlined style={{ color: '#722ed1', fontSize: '20px' }} />,
      items: [
        { description: 'Create file controller for updating user profile pics', importance: 'medium' }, // medium priority
        { description: 'Use cloud services to store files', importance: 'low' }, // low priority
      ],
    },
    {
      section: 'Frontend',
      icon: <FileOutlined style={{ color: '#fa8c16', fontSize: '20px' }} />,
      items: [
        { description: 'Create an "Update Profile" button', importance: 'high' },
      ],
    },
    {
      section: 'Deployment',
      icon: <DeploymentUnitOutlined style={{ color: '#13c2c2', fontSize: '20px' }} />,
      items: [
        { description: 'Faster deployment', importance: 'low' },
      ],
    },
  ];

  // Function to get the appropriate icon based on task importance
  const getImportanceIcon = (importance: string) => {
    switch (importance) {
      case 'high':
        return <ExclamationCircleOutlined style={{ color: 'red', fontSize: '20px' }} />;
      case 'medium':
        return <WarningOutlined style={{ color: 'orange', fontSize: '20px' }} />;
      case 'low':
        return <CheckCircleOutlined style={{ color: 'green', fontSize: '20px' }} />;
      default:
        return null;
    }
  };

  return (
    <PageContainer>
      <Layout style={{ padding: '20px', background: '#f0f2f5' }}>
        <Content style={{ maxWidth: '800px', margin: '0 auto' }}>
          <Card
            style={{
              borderRadius: '8px',
              boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
              background: '#ffffff',
            }}
            bodyStyle={{ padding: '30px' }}
          >
            <Title level={2} style={{ textAlign: 'center', marginBottom: '20px' }}>
              Todo List 📝
            </Title>

            <Paragraph style={{ textAlign: 'center', marginBottom: '30px', fontSize: '16px', color: '#595959' }}>
              Here's a list of improvements I plan to make for this web application. Feel free to follow along or suggest new tasks!
            </Paragraph>

            {/* Section List */}
            {tasks.map((taskSection, index) => (
              <Card
                key={index}
                bordered={false}
                style={{
                  background: '#f6ffed',
                  marginBottom: '20px',
                  borderRadius: '8px',
                  padding: '20px',
                }}
              >
                <Title
                  level={4}
                  style={{
                    marginBottom: '10px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    color: '#52c41a',
                  }}
                >
                  {taskSection.icon}
                  {taskSection.section}
                </Title>
                <List
                  itemLayout="horizontal"
                  dataSource={taskSection.items}
                  renderItem={(item) => (
                    <List.Item
                      actions={[
                        <Tag color="default" style={{ width: '150px', textAlign: 'center', display: 'flex', justifyContent: 'center' }}>
                          {getImportanceIcon(item.importance)} {item.importance.charAt(0).toUpperCase() + item.importance.slice(1)} Priority
                        </Tag>,
                      ]}
                    >
                      <List.Item.Meta title={item.description} />
                    </List.Item>
                  )}
                />
              </Card>
            ))}

            <Paragraph style={{ textAlign: 'center', marginTop: '30px' }}>
              Want to suggest a new task? Email me at{' '}
              <Button
                type="link"
                href="mailto:jxh186045@gmail.com"
                icon={<CloudOutlined />}
                style={{ fontSize: '16px', padding: 0 }}
              >
                jxh186045@gmail.com
              </Button>
            </Paragraph>
          </Card>
        </Content>
      </Layout>
    </PageContainer>
  );
};

export default Todo;
