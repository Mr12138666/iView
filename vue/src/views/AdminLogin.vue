<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <div class="brand-panel">
        <div class="brand-mark">IVF</div>
        <h1>智面未来管理后台</h1>
        <p>管理员专属入口，用于系统配置、广告位、用户与职位管理。</p>
      </div>

      <div class="login-panel">
        <h2>管理员登录</h2>
        <p>请使用管理员账号进入后台</p>
        <el-form ref="formRef" :model="data.form" :rules="data.rules" @keyup.enter="login">
          <el-form-item prop="username">
            <el-input v-model="data.form.username" size="large" placeholder="请输入管理员账号" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="data.form.password" show-password size="large" placeholder="请输入密码" />
          </el-form-item>
          <el-button type="primary" size="large" class="login-button" @click="login">登录后台</el-button>
        </el-form>
        <el-button link class="front-link" @click="router.push('/login')">返回普通登录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'
import router from '@/router/index.js'

const formRef = ref()

const data = reactive({
  form: {
    username: '',
    password: '',
    role: 'ADMIN'
  },
  rules: {
    username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
})

const login = () => {
  formRef.value.validate(valid => {
    if (!valid) return
    request.post('/login', data.form).then(res => {
      if (res?.code === '200' && res.data?.role === 'ADMIN') {
        ElMessage.success('登录成功')
        localStorage.setItem('xm-user', JSON.stringify(res.data))
        router.push('/manager/home')
      } else {
        ElMessage.error(res?.msg || '管理员账号或密码错误')
      }
    }).catch(() => {
      ElMessage.error('登录失败，请检查后端服务')
    })
  })
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
      radial-gradient(circle at 20% 20%, rgba(92, 189, 185, .34), transparent 30%),
      linear-gradient(135deg, #071827, #102b3a 48%, #173f3d);
}

.admin-login-card {
  width: min(920px, 100%);
  min-height: 520px;
  display: grid;
  grid-template-columns: 1fr 420px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, .12);
  border-radius: 18px;
  background: rgba(255, 255, 255, .94);
  box-shadow: 0 24px 70px rgba(0, 0, 0, .28);
}

.brand-panel {
  padding: 56px;
  color: #fff;
  background: linear-gradient(145deg, rgba(7, 24, 39, .96), rgba(20, 84, 82, .92));
}

.brand-mark {
  width: 64px;
  height: 64px;
  display: grid;
  place-items: center;
  margin-bottom: 42px;
  border-radius: 16px;
  background: #5cbdb9;
  font-weight: 800;
  letter-spacing: 1px;
}

.brand-panel h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.25;
}

.brand-panel p {
  max-width: 360px;
  margin-top: 18px;
  color: rgba(255, 255, 255, .72);
  line-height: 1.8;
}

.login-panel {
  padding: 70px 46px;
}

.login-panel h2 {
  margin: 0;
  color: #1f2d3d;
  font-size: 28px;
}

.login-panel p {
  margin: 10px 0 30px;
  color: #6b7280;
}

.login-button {
  width: 100%;
  height: 46px;
  margin-top: 8px;
  background-color: #5cbdb9;
  border-color: #5cbdb9;
}

.front-link {
  width: 100%;
  margin-top: 18px;
  color: #5cbdb9;
}

@media (max-width: 760px) {
  .admin-login-card {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 34px;
  }

  .login-panel {
    padding: 36px 28px;
  }
}
</style>
