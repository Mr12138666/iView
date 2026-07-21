import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL,
    timeout: 30000  // 后台接口超时时间
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    let user = JSON.parse(localStorage.getItem("xm-user") || '{}')
    config.headers['token'] = user.token || ''
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            try {
                res = res ? JSON.parse(res) : res
            } catch (parseError) {
                console.error('响应数据解析失败', parseError)
            }
        }
        // 当权限验证不通过的时候给出提示
        if (res?.code === '401') {
            ElMessage.error(res.msg)
            router.push('/login')
        }
        return res;
    },
    error => {
        const status = error.response?.status
        if (status === 401) {
            ElMessage.error('登录状态已失效，请重新登录')
            router.push('/login')
        } else if (status === 404) {
            ElMessage.error('未找到请求接口')
        } else if (status === 500) {
            ElMessage.error('系统异常，请查看后端控制台报错')
        } else if (!error.response) {
            console.error('请求未能连接到服务端', error.message)
        } else {
            console.error(error.message)
        }
        return Promise.reject(error)
    }
)

export default request
