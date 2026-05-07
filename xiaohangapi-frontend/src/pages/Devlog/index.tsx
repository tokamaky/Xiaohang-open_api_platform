import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { Layout, Card, Typography, Tag, Collapse, Timeline, Divider } from 'antd';
import {
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  RobotOutlined,
  RocketOutlined,
  ToolOutlined,
  ScheduleOutlined,
  DatabaseOutlined,
  CloudOutlined,
  ApiOutlined,
  HistoryOutlined,
} from '@ant-design/icons';
import './index.less';

const { Content } = Layout;
const { Title, Text, Paragraph } = Typography;
const { Panel } = Collapse;

interface FeatureItem {
  title: string;
  description: string;
  tag?: string;
  tagColor?: string;
}

interface Phase {
  key: string;
  phase: string;
  date: string;
  title: string;
  subtitle: string;
  icon: React.ReactNode;
  color: string;
  features: FeatureItem[];
  learnings: string[];
}

const phases: Phase[] = [
  {
    key: 'phase-1',
    phase: 'Phase 1',
    date: '2026/04/15',
    title: 'Distributed Rate Limiting',
    subtitle: 'Gateway layer security & traffic protection',
    icon: <SafetyCertificateOutlined />,
    color: '#00D4AA',
    features: [
      {
        title: 'Three-Dimensional Rate Limiting',
        description:
          'Implemented sliding window rate limiting across three dimensions: global (platform-wide), per-user (by accessKey), and per-interface (by path). Each dimension serves a distinct purpose — global limits protect the platform from traffic spikes, per-user limits prevent resource monopolization, and per-interface limits protect individual APIs from being overwhelmed.',
        tag: 'Redis + Lua',
        tagColor: '#FF6B6B',
      },
      {
        title: 'Sliding Window Algorithm with Redis ZSET',
        description:
          'Used Redis sorted sets (ZSET) where each request is stored as a member with its timestamp as the score. The sliding window removes expired entries and counts remaining entries atomically via a Lua script. This approach is more accurate than fixed-window counters and handles burst traffic gracefully without the "boundary reset" problem.',
        tag: 'Redis ZSET',
        tagColor: '#4ECDC4',
      },
      {
        title: 'Atomic Lua Script Execution',
        description:
          'Implemented the sliding window logic as a Redis Lua script that runs atomically. This guarantees accuracy under high concurrency — no race conditions between reading the count and incrementing it. The script removes stale entries, counts current requests in the window, and adds a new entry only if under the limit, all in a single atomic operation.',
        tag: 'Lua Script',
        tagColor: '#45B7D1',
      },
      {
        title: 'Fail-Open Architecture',
        description:
          'When Redis is unavailable (connection timeout or error), the rate limiter returns true and allows the request through. This ensures the platform remains available even when the rate limiting subsystem fails, following the "fail open" principle for non-critical security features. The trade-off is that during Redis outages, rate limiting is temporarily bypassed.',
        tag: 'Resilience',
        tagColor: '#F0B429',
      },
      {
        title: 'HTTP 429 Response with Retry-After Header',
        description:
          'When rate limit is triggered, the Gateway returns HTTP 429 Too Many Requests with a JSON body containing error code 42900 and a descriptive message. A Retry-After: 60 header tells the client how many seconds to wait before retrying. The GlobalFilterExceptionHandler was enhanced to map error codes to appropriate HTTP status codes.',
        tag: 'HTTP 429',
        tagColor: '#96CEB4',
      },
      {
        title: 'Three-Tier Gateway Filter Chain',
        description:
          'Integrate rate limiting into the existing Gateway filter chain at the correct position: after signature authentication (to know the user) but before routing to the interface service. This ensures we have the accessKey available for per-user limiting and the path for per-interface limiting, while avoiding unnecessary checks for invalid requests.',
        tag: 'Spring Cloud Gateway',
        tagColor: '#9B59B6',
      },
    ],
    learnings: [
      'Redis is not just a cache — its data structures (STRING, HASH, ZSET, LIST, SET) enable sophisticated distributed algorithms. The ZSET score-based sliding window is far superior to INCR + EXPIRE for rate limiting.',
      'Lua scripts in Redis are evaluated atomically as a single operation. This eliminates the need for distributed locks when implementing counters. Every rate limiting implementation should use Lua scripts for correctness under concurrency.',
      '"Fail-open" vs "fail-closed" is a critical design decision. For security features like rate limiting, failing open keeps the service available but temporarily unprotected. For payment systems, failing closed is better. Always make this decision consciously.',
      'The sliding window algorithm solves the boundary reset problem of fixed-window counters. At 12:00:59, a fixed window would reset, allowing a burst of 2x the rate. Sliding windows smoothly transition and prevent this.',
      'Three dimensions of rate limiting (global/user/interface) give fine-grained control. A single global limit is too coarse — one abusive user could block all other users. Per-user + per-interface provides fairness and protection.',
    ],
  },
  {
    key: 'phase-2',
    phase: 'Phase 2',
    date: '2026/04/27',
    title: 'Circuit Breaker & Fallback',
    subtitle: 'Resilient fault tolerance for downstream service failures',
    icon: <ThunderboltOutlined />,
    color: '#FF6B6B',
    features: [
      {
        title: 'Sentinel-Based Circuit Breaker Pattern',
        description:
          'Integrated Alibaba Sentinel as the circuit breaker engine into the Spring Cloud Gateway. Sentinel monitors the QPS and response time of each downstream interface service in real-time. When a service\'s error rate or average response time exceeds the configured threshold, the circuit breaker trips — subsequent requests are blocked immediately instead of being forwarded, preventing cascading failures from exhausting resources.',
        tag: 'Sentinel',
        tagColor: '#FF6B6B',
      },
      {
        title: 'Three-State Machine: Closed / Open / Half-Open',
        description:
          'The circuit breaker implements the three-state resilience pattern. In the Closed state, requests flow normally. When the failure threshold is breached, it transitions to Open — all requests are rejected with a fallback response. After a configurable sleep window, it moves to Half-Open, allowing a limited number of test requests through. If those succeed, it returns to Closed; if they fail, it returns to Open. This state machine prevents the "thundering herd" problem and gives the downstream service time to recover.',
        tag: 'State Machine',
        tagColor: '#F0B429',
      },
      {
        title: 'Slow-Call Detection with Adaptive Thresholds',
        description:
          'Beyond simple error-rate-based tripping, Sentinel tracks the slow-call ratio — the percentage of calls whose response time exceeds a threshold (e.g., 2000ms). Slow calls consume connection pool slots and thread resources without delivering value. By combining slow-call ratio with error rate as dual triggers, the circuit breaker handles both crash failures and degradation failures. The thresholds are configurable per downstream service, reflecting their individual SLA requirements.',
        tag: 'Adaptive Threshold',
        tagColor: '#4ECDC4',
      },
      {
        title: 'Graceful Fallback Responses',
        description:
          'When the circuit breaker is Open, requests are redirected to a fallback handler instead of being silently dropped. The fallback returns a structured JSON response with a descriptive error code (e.g., 50300 for service unavailable) and a human-readable message explaining that the downstream service is temporarily unreachable. This gives API consumers the information they need to implement their own retry logic or degradation behavior on the client side.',
        tag: 'Fallback',
        tagColor: '#45B7D1',
      },
      {
        title: 'Gateway-Level Global Exception Handling',
        description:
          'Enhanced the GlobalFilterExceptionHandler to intercept Sentinel\'s BlockException (thrown when the circuit breaker blocks a request) alongside existing Spring Cloud Gateway exceptions. All blocked requests are logged with the downstream service identifier, the current circuit breaker state, and the reason for blocking — providing observability into which services are failing and how often, without requiring additional logging infrastructure.',
        tag: 'Exception Handler',
        tagColor: '#9B59B6',
      },
      {
        title: 'Per-Route Circuit Breaker Configuration',
        description:
          'Each downstream interface service (route) in the Gateway has its own independent circuit breaker instance with configurable parameters: slow-call-threshold duration, slow-call-ratio threshold, error-ratio threshold, error-count threshold, and sleep-window duration. This allows fine-grained control — a latency-sensitive service might have a 500ms slow-call threshold, while a batch-processing service tolerates 5 seconds. The configuration is loaded from application.yml, making it easy to tune without redeploying.',
        tag: 'Per-Route Config',
        tagColor: '#96CEB4',
      },
    ],
    learnings: [
      'Circuit breakers are not just about handling crashes — they handle degradation too. A service that returns slow responses is as dangerous as one that crashes, because it holds onto threads and connections. Monitor both error rates and slow-call ratios.',
      'The Half-Open state is the most subtle and important part of a circuit breaker. Too short a sleep window and the downstream service hasn\'t recovered; too long and clients suffer unnecessary delays. Start conservative and adjust based on the downstream\'s actual recovery time.',
      'Fallback responses are a contract with your API consumers. Always return a structured, documented error response instead of an empty or opaque one. Clients need to know whether the failure is transient, so they can decide whether to retry.',
      'Sentinel\'s approach of running as a library (embedded in the Gateway process) rather than a separate sidecar avoids network latency overhead for rule checks. The trade-off is that rule updates require a push mechanism — Sentinel provides a DynamicIsolateFlowChecker for this.',
      'Circuit breaking and rate limiting are complementary: rate limiting protects against traffic spikes and resource exhaustion, while circuit breaking protects against downstream failures. Together they form the two pillars of gateway resilience.',
    ],
  },
  {
    key: 'phase-3',
    phase: 'Phase 3',
    date: '2026/04/30',
    title: 'GitHub OAuth Integration',
    subtitle: 'Seamless social login with account linking support',
    icon: <RobotOutlined />,
    color: '#00A3FF',
    features: [
      {
        title: 'Stateless OAuth 2.0 Flow with CSRF Protection',
        description:
          'Implemented a server-side GitHub OAuth 2.0 flow without relying on Spring Security\'s built-in session-based OAuth login. Each authorization request generates a cryptographically random 32-digit CSRF token (state parameter) and encodes the frontend return URL into the state, preventing cross-site request forgery attacks. The state parameter is passed through GitHub\'s redirect and decoded on callback to restore the original navigation context.',
        tag: 'OAuth 2.0 + CSRF',
        tagColor: '#FF6B6B',
      },
      {
        title: 'Token Exchange & User Info Retrieval',
        description:
          'After GitHub redirects back with an authorization code, the backend exchanges it for an access token via a POST to GitHub\'s token endpoint (client_id + client_secret + code). The access token is then used to fetch the authenticated user\'s public profile from GitHub\'s /user endpoint — retrieving the GitHub ID, login name, and avatar URL — entirely server-side without exposing credentials to the browser.',
        tag: 'Token Exchange',
        tagColor: '#4ECDC4',
      },
      {
        title: 'Account Linking for Existing Users',
        description:
          'When a logged-in user clicks "Link GitHub", the OAuth flow binds their existing account to a GitHub ID without creating a new account. The backend detects the existing session (via JWT cookie), updates the user record with the GitHub ID, and returns the updated user profile. If the GitHub account is already linked to a different user, an error is thrown to prevent account hijacking.',
        tag: 'Account Binding',
        tagColor: '#F0B429',
      },
      {
        title: 'Auto-Registration for New Users',
        description:
          'If no existing session and no existing account matches the GitHub ID, the system auto-registers a new account: generating a username from the GitHub login name, setting a random avatar from GitHub, and issuing a JWT token. The new user lands on the frontend already authenticated — no password required, no friction. The GitHub ID is stored as a unique column in the User entity, ensuring one GitHub account maps to one platform account.',
        tag: 'Auto-Register',
        tagColor: '#45B7D1',
      },
      {
        title: 'Session-Based OAuth Result Relay',
        description:
          'Since OAuth callbacks are server-side redirects (not fetch/XHR), the OAuth result cannot be returned directly to the frontend via a response body. Instead, the backend stores the LoginUserVO in the HTTP session after processing the callback, then redirects to the frontend URL with a ?__oauth_done=1 marker. The frontend polls /api/oauth/github/result to retrieve the session-stored result, completing the round-trip transparently.',
        tag: 'Session Relay',
        tagColor: '#9B59B6',
      },
      {
        title: 'Flexible Callback URL with Environment Variable',
        description:
          'The GitHub OAuth callback URL is constructed dynamically using a BACKEND_HOST environment variable, supporting multiple deployment environments (local, Railway production) without code changes. The host string is normalized — stripping any leading protocol prefix or trailing slash — before being reconstructed as https://host/api/oauth/github/callback to match the registered GitHub App redirect URI exactly.',
        tag: 'Env-Driven Config',
        tagColor: '#96CEB4',
      },
    ],
    learnings: [
      'The OAuth state parameter is not just a CSRF token — it can carry payload. Encoding the frontend redirect URL into state avoids the need for a separate session lookup to restore navigation context after the callback.',
      'Social login UX has three distinct cases: (1) existing user linking a new identity, (2) returning user logging in with social account, (3) new user registering via social account. All three must be handled correctly or users get frustrated or accounts get duplicated.',
      'GitHub\'s redirect_uri matching is strict — it requires an exact match including protocol and path. A trailing slash or www vs non-www difference will cause a redirect_uri mismatch error. Always normalize the callback URL before registration and use environment variables to manage per-environment URIs.',
      'OAuth callbacks are HTTP redirects, not API calls. This means the backend cannot return JSON to the frontend directly — you need a relay mechanism. Session storage + frontend polling is the simplest approach; WebSocket or postMessage could be alternatives for SPA-specific flows.',
      'Storing githubId as a nullable unique column on the User table works cleanly for both password-based and OAuth-only accounts. Just ensure the uniqueness constraint handles NULL values correctly (MySQL treats multiple NULLs as non-conflicting in unique indexes).',
    ],
  },
  {
    key: 'phase-4',
    phase: 'Phase 4',
    date: '2026/05/06',
    title: 'CI/CD Pipeline & Testing Infrastructure',
    subtitle: 'End-to-end automation from code to production deployment',
    icon: <CloudOutlined />,
    color: '#7C3AED',
    features: [
      {
        title: 'GitHub Actions CI Pipeline',
        description:
          'Implemented a comprehensive CI pipeline with five parallel jobs: code quality analysis via SonarQube, Maven build, unit tests with MySQL/Redis services, OWASP security scanning, and Docker image building. The pipeline triggers on push and pull requests to main/develop/feature branches, with Maven caching for faster builds and JaCoCo coverage reports uploaded to Codecov.',
        tag: 'GitHub Actions',
        tagColor: '#FF6B6B',
      },
      {
        title: 'Multi-Environment CD Pipeline',
        description:
          'Designed a staged deployment pipeline that automatically deploys to Staging on develop branch merges and Production on main branch merges. The CD workflow waits for CI completion via workflow_run trigger, ensuring only passing builds reach deployment. Each environment uses independent Kubernetes configurations and secrets.',
        tag: 'CD Pipeline',
        tagColor: '#4ECDC4',
      },
      {
        title: 'Blue-Green Deployment Strategy',
        description:
          'Implemented zero-downtime production deployments using a blue-green strategy. The new deployment (green) is provisioned alongside the current one (blue), health-checked, then traffic is switched via a single kubectl patch command. If anything goes wrong, traffic can be reverted instantly by switching back to blue.',
        tag: 'Blue-Green',
        tagColor: '#F0B429',
      },
      {
        title: 'Kubernetes Deployment Configuration',
        description:
          'Created Kubernetes manifests for backend, gateway, and interface services with proper resource limits, health checks (liveness/readiness probes), and environment-specific configurations. The overlay structure allows environment-specific customization while sharing common base templates.',
        tag: 'Kubernetes',
        tagColor: '#45B7D1',
      },
      {
        title: 'Helm Chart Deployment Support',
        description:
          'Added Helm chart support for flexible deployments via workflow_dispatch trigger. Users can manually deploy to any environment with custom image tags, enabling hotfixes and emergency deployments without merging code. The atomic flag ensures automatic rollback on failure.',
        tag: 'Helm Charts',
        tagColor: '#9B59B6',
      },
      {
        title: 'Comprehensive Unit Test Coverage',
        description:
          'Added unit tests across service layers (UserService, InterfaceInfoService), utilities (SqlUtils), circuit breaker resilience, and exception handling. Tests use Mockito for dependency injection without database coupling, enabling fast, reliable test execution in CI. SQL injection prevention was explicitly tested with various attack vectors.',
        tag: 'Unit Testing',
        tagColor: '#96CEB4',
      },
      {
        title: 'SBOM Generation & Security Compliance',
        description:
          'Integrated Anchore SBOM action to generate SPDX-formatted Software Bill of Materials for each Docker image. SBOMs capture all dependencies and their versions, enabling vulnerability tracking and compliance reporting. Artifacts are retained for 365 days.',
        tag: 'SBOM',
        tagColor: '#00D4AA',
      },
      {
        title: 'Slack Deployment Notifications',
        description:
          'Added Slack webhook integration to notify the team of deployment status. Notifications include commit SHA, author, branch, and workflow name in a structured format, enabling the team to track what changed and who deployed it without checking GitHub.',
        tag: 'Slack Notify',
        tagColor: '#E84C5A',
      },
      {
        title: 'Database Backup Before Production Deployments',
        description:
          'Implemented automatic MySQL backup before production deployments using kubectl exec to run mysqldump inside the production pod. Backups are timestamped and stored, providing a recovery point before any breaking changes. The backup step uses continue-on-error to ensure it never blocks deployment.',
        tag: 'DB Backup',
        tagColor: '#F0B429',
      },
    ],
    learnings: [
      'workflow_run triggers run in a separate context with limited access to the triggering workflow\'s artifacts. Always upload what you need in the CI workflow, or use a composite action to share logic between pipelines.',
      'Blue-green deployments require your application to handle multiple versions running simultaneously — specifically, database migrations must be backward-compatible. If you need destructive changes, use rolling updates instead.',
      'Kubernetes health probes (liveness/readiness) are not optional for production. Without them, Kubernetes will route traffic to pods that are starting, crashing, or overloaded, causing unpredictable failures.',
      'Maven test caching with mvn test -Dspring.profiles.active=test works well, but remember that test containers (MySQL, Redis) still need to start. Use container health checks to avoid flaky tests from services that haven\'t finished initializing.',
      'SBOM generation adds minimal build time but provides massive security value. In a breach scenario, you can immediately query which images contain a vulnerable dependency instead of playing archaeology across multiple repositories.',
    ],
  },
];

const Changelog: React.FC = () => {
  return (
    <PageContainer>
      <Layout style={{ padding: '24px', background: 'transparent' }}>
        <Content style={{ maxWidth: '900px', margin: '0 auto' }} className="changelog-page">
          {/* Page Header */}
          <Card className="cl-header-card" bodyStyle={{ padding: '32px 36px' }}>
            <div className="cl-header">
              <div className="cl-header-icon">
                <HistoryOutlined />
              </div>
              <div className="cl-header-text">
                <Title level={2} className="cl-main-title">
                  Developer Devlog
                </Title>
                <Paragraph className="cl-subtitle">
                  Tracking the evolution of this platform — new features, technical deep-dives, and
                  lessons learned along the way.
                </Paragraph>
              </div>
            </div>
            <Divider className="cl-header-divider" />
            <div className="cl-header-stats">
              <div className="cl-stat">
                <span className="cl-stat-number">4</span>
                <span className="cl-stat-label">Phase Completed</span>
              </div>
              <div className="cl-stat-sep" />
              <div className="cl-stat">
                <span className="cl-stat-number">27</span>
                <span className="cl-stat-label">Features Added</span>
              </div>
              <div className="cl-stat-sep" />
              <div className="cl-stat">
                <span className="cl-stat-number">20</span>
                <span className="cl-stat-label">Key Learnings</span>
              </div>
            </div>
          </Card>

          {/* Phase Cards */}
          <div className="cl-phases-container">
            {phases.map((phase, index) => (
              <Card
                key={phase.key}
                className={`cl-phase-card cl-phase-${index + 1}`}
                bodyStyle={{ padding: '0' }}
              >
                {/* Phase Header */}
                <div className="cl-phase-header" style={{ borderLeftColor: phase.color }}>
                  <div className="cl-phase-meta">
                    <Tag
                      className="cl-phase-tag"
                      style={{ background: `${phase.color}15`, borderColor: `${phase.color}40`, color: phase.color }}
                    >
                      {phase.phase}
                    </Tag>
                    <span className="cl-phase-date">
                      <ScheduleOutlined /> {phase.date}
                    </span>
                  </div>
                  <div className="cl-phase-title-row">
                    <span className="cl-phase-icon" style={{ color: phase.color }}>
                      {phase.icon}
                    </span>
                    <Title level={3} className="cl-phase-title">
                      {phase.title}
                    </Title>
                  </div>
                  <Paragraph className="cl-phase-subtitle">{phase.subtitle}</Paragraph>
                </div>

                {/* Features Accordion */}
                <div className="cl-phase-body">
                  <Collapse
                    bordered={false}
                    defaultActiveKey={['feature-0']}
                    expandIconPosition="end"
                    className="cl-features-collapse"
                  >
                    {phase.features.map((feature, fIdx) => (
                      <Panel
                        key={`feature-${fIdx}`}
                        header={
                          <div className="cl-feature-header">
                            <span className="cl-feature-num">{String(fIdx + 1).padStart(2, '0')}</span>
                            <span className="cl-feature-title">{feature.title}</span>
                            {feature.tag && (
                              <Tag className="cl-feature-tag" style={{ background: `${feature.tagColor}15`, borderColor: `${feature.tagColor}30`, color: feature.tagColor }}>
                                {feature.tag}
                              </Tag>
                            )}
                          </div>
                        }
                      >
                        <Paragraph className="cl-feature-desc">{feature.description}</Paragraph>
                      </Panel>
                    ))}
                  </Collapse>
                </div>

                {/* Learnings Section */}
                <div className="cl-learnings-section">
                  <div className="cl-learnings-header">
                    <RocketOutlined className="cl-learnings-icon" />
                    <span>Technical Insights & Learnings</span>
                  </div>
                  <Timeline
                    className="cl-learnings-timeline"
                    items={phase.learnings.map((learning, lIdx) => ({
                      key: lIdx,
                      dot: (
                        <div className="cl-learning-dot" style={{ borderColor: phase.color }}>
                          {lIdx + 1}
                        </div>
                      ),
                      children: (
                        <Paragraph className="cl-learning-text">{learning}</Paragraph>
                      ),
                    }))}
                  />
                </div>
              </Card>
            ))}
          </div>

          {/* Future Phases Placeholder */}
          <Card className="cl-future-card" bodyStyle={{ padding: '32px' }}>
            <div className="cl-future-header">
              <div className="cl-future-icon">
                <ScheduleOutlined />
              </div>
              <Title level={4} className="cl-future-title">
                More Phases Coming
              </Title>
            </div>
            <Paragraph className="cl-future-desc">
              Phase 5 is planned and will follow the same structure: a
              problem statement, implementation details, and real technical insights gained
              from building.
            </Paragraph>
            <div className="cl-future-list">
              <div className="cl-future-item">
                <span className="cl-future-item-num">05</span>
                <span className="cl-future-item-name">SkyWalking Distributed Tracing</span>
                <Tag color="default">Planned</Tag>
              </div>
            </div>
          </Card>
        </Content>
      </Layout>
    </PageContainer>
  );
};

export default Changelog;
