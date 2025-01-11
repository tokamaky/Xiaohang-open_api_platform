export default [
  { name: 'Please Read', icon: 'ExclamationCircleOutlined', path: '/', component: './Infomation/PleaseRead' },
  { name: 'API Store', icon: 'HomeOutlined', path: '/MyApi', component: './Index' },
  { name: 'My API', icon: 'StarOutlined', path: '/my_interface', component: './MyInterface' },
  {
    name: 'Check API',
    icon: 'smile',
    path: '/interface_info/:id',
    component: './InterfaceInfo',
    hideInMenu: true,
  },
  {
    path: '/user',
    layout: false,
    routes: [{ name: 'Login', path: '/user/login', component: './User/Login' }],
  },
  {
    path: '/admin',
    name: 'Management Center',
    icon: 'crown',
    access: 'canAdmin',
    routes: [
      {
        name: 'API Management',
        icon: 'table',
        path: '/admin/interface_info',
        component: './Admin/InterfaceInfo',
      },
      {
        name: 'Invocation Count Statistics',
        icon: 'table',
        path: '/admin/analysis',
        component: './Admin/InterfaceInfoAnalysis',
      },
    ],
  },
  { name: 'User Center', icon: 'UserOutlined', path: '/profile', component: './User/Profile' },
  { name: 'Author To Do', icon: 'RobotOutlined', path: '/Todo', component: './Infomation/Todo' },
  { path: '*', layout: false, component: './404' },
];
