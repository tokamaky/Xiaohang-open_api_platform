import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { Layout, Typography, Card, Image, Button } from 'antd';
import { MailOutlined, InfoCircleOutlined, PictureOutlined, SmileOutlined } from '@ant-design/icons';
import logicImg from '../../../public/Open_Api_Plateform_UML.png';

const { Content } = Layout;
const { Title, Paragraph } = Typography;

const PleaseRead: React.FC = () => {
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
              Welcome to Panda API! 🐼
            </Title>

            {/* Section 1: Why Things Might Not Update Instantly */}
            <Card
              bordered={false}
              style={{
                background: '#e6f7ff',
                marginBottom: '20px',
                borderRadius: '8px',
                padding: '20px',
              }}
            >
              <Title
                level={4}
                style={{
                  marginBottom: '10px',
                  color: '#1890ff',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <InfoCircleOutlined />
                Why Things Might Not Update Instantly
              </Title>
              <Paragraph>
                Due to the high deployment fees, I’ve opted for a slower server. This means:
              </Paragraph>
              <Paragraph>
                <b>1.</b> The <b>Remaining Number of Invocations</b> and <b>Number of Invocations</b> may not
                update immediately after you invoke. However, the numbers will be correct the next time you open
                the web app. Thank you for your patience! 😊
              </Paragraph>
              <Paragraph>
                <b>2.</b> The first time you click "Invoke" in the <b>Online Invocation</b> section, you might
                encounter an error. This is due to the slower server speed. Don’t worry, just try again! 🚀
              </Paragraph>
            </Card>

            {/* Section 2: Logic Diagram */}
            <Card
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
                  color: '#52c41a',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <PictureOutlined />
                Logic Diagram
              </Title>
              <Paragraph>
                Below is a visual representation of the logic behind the system. This helps to clarify the flow of
                operations.
              </Paragraph>
              <Image
                src={logicImg} // Replace with the actual image path
                alt="Logic Diagram"
                preview={true}
                style={{
                  display: 'block',
                  margin: '20px auto',
                  borderRadius: '8px',
                  maxWidth: '100%',
                }}
              />
            </Card>

            {/* Section 3: Feedback and Suggestions */}
            <Card
              bordered={false}
              style={{
                background: '#fffbe6',
                marginBottom: '20px',
                borderRadius: '8px',
                padding: '20px',
              }}
            >
              <Title
                level={4}
                style={{
                  marginBottom: '10px',
                  color: '#faad14',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <SmileOutlined />
                Feedback and Suggestions
              </Title>
              <Paragraph>
                I’m always looking to improve this application! If you have any ideas or encounter issues,
                please don’t hesitate to reach out. You can email me at:
              </Paragraph>
              <Button
                type="link"
                href="mailto:jxh186045@gmail.com"
                icon={<MailOutlined />}
                style={{ fontSize: '16px', padding: 0 }}
              >
                jxh186045@gmail.com
              </Button>
              <Paragraph>
                Your feedback means the world to me! 💌
              </Paragraph>
            </Card>

            <Paragraph style={{ textAlign: 'center', marginTop: '30px' }}>
              Thank you for your understanding and support! 🎉
            </Paragraph>
          </Card>
        </Content>
      </Layout>
    </PageContainer>
  );
};

export default PleaseRead;
