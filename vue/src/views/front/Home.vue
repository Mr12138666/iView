<template>
  <main class="talent-home">
    <section class="hero-shell">
      <div class="hero-copy">
        <div class="eyebrow">AI recruitment cockpit</div>
        <h1>智面未来</h1>
        <p class="hero-desc">从岗位发现到模拟面试，把求职准备压缩进一个更清晰的智能工作台。</p>

        <div class="search-console">
          <el-input
              v-model="data.name"
              size="large"
              placeholder="搜索职位、技术栈或公司"
              @keyup.enter="search"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button size="large" type="primary" :icon="Search" @click="search">搜索</el-button>
          <el-button size="large" class="ghost-action" :icon="VideoCamera" @click="navTo('/front/aimock')">
            AI 面试
          </el-button>
        </div>

        <div class="signal-row">
          <div class="signal-item">
            <span>{{ data.industryData.length || '--' }}</span>
            <small>行业雷达</small>
          </div>
          <div class="signal-item">
            <span>{{ data.positionData.length || '--' }}</span>
            <small>热招岗位</small>
          </div>
          <div class="signal-item">
            <span>24h</span>
            <small>智能陪练</small>
          </div>
        </div>
      </div>

      <div class="radar-panel">
        <div class="panel-header">
          <span>Opportunity stream</span>
          <i></i>
        </div>
        <div class="ad-grid">
          <button class="ad-cell ad-left" type="button" @click="navToAd(data.leftAd)">
            <img v-if="data.leftAd.img" :src="data.leftAd.img" alt="">
            <span v-else>左侧大图</span>
          </button>
          <button class="ad-cell ad-main" type="button" @click="navToAd(data.centerAd)">
            <img v-if="data.centerAd.img" :src="data.centerAd.img" alt="">
            <span v-else>中心大图</span>
          </button>
          <button class="ad-cell ad-right" type="button" @click="navToAd(data.rightAd)">
            <img v-if="data.rightAd.img" :src="data.rightAd.img" alt="">
            <span v-else>右侧大图</span>
          </button>
          <button class="ad-cell ad-small" type="button" @click="navToAd(data.leftDownAd)">
            <img v-if="data.leftDownAd.img" :src="data.leftDownAd.img" alt="">
            <span v-else>左侧小图</span>
          </button>
          <button class="ad-cell ad-wide" type="button" @click="navToAd(data.centerDownAd)">
            <img v-if="data.centerDownAd.img" :src="data.centerDownAd.img" alt="">
            <span v-else>中心小图</span>
          </button>
          <button class="ad-cell ad-small" type="button" @click="navToAd(data.rightDownAd)">
            <img v-if="data.rightDownAd.img" :src="data.rightDownAd.img" alt="">
            <span v-else>右侧小图</span>
          </button>
        </div>
      </div>
    </section>

    <section class="position-section">
      <div class="section-title">
        <div>
          <div class="eyebrow">Live hiring feed</div>
          <h2>热招职位</h2>
        </div>
        <el-button class="ghost-action" :icon="Monitor" @click="navTo('/front/interviewHistory')">
          面试记录
        </el-button>
      </div>

      <el-tabs v-model="data.activeName" class="industry-tabs" @tab-change="handleClick">
        <el-tab-pane v-for="item in data.industryData" :key="item.id" :label="item.name" :name="item.id">
          <div class="job-grid">
            <article
                v-for="it in data.positionData"
                :key="it.id"
                class="job-card"
                @click="navTo('/front/positionDetail?id=' + it.id)"
            >
              <div class="job-topline">
                <div>
                  <h3>{{ it.name }}</h3>
                  <p>{{ it.employName }}</p>
                </div>
                <strong>{{ it.salary }}</strong>
              </div>
              <div class="tag-list">
                <el-tag v-for="tag in it.tagList" :key="tag" effect="dark">{{ tag }}</el-tag>
              </div>
              <div class="job-footer">
                <img :src="it.employAvatar" alt="">
                <span>{{ item.name }}</span>
                <span>{{ it.employStage }}</span>
                <el-icon><TrendCharts /></el-icon>
              </div>
            </article>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>
  </main>
</template>

<script setup>
import { reactive } from 'vue'
import { Monitor, Search, TrendCharts, VideoCamera } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'

const data = reactive({
  name: null,
  advertiseData: [],
  centerAd: {},
  leftAd: {},
  rightAd: {},
  centerDownAd: {},
  leftDownAd: {},
  rightDownAd: {},
  activeName: null,
  industryData: [],
  positionData: []
})

const loadAdvertise = () => {
  request.get('/advertise/selectAll').then(res => {
    if (res.code === '200') {
      data.advertiseData = res.data || []
      data.centerAd = findAd('中心大图')
      data.leftAd = findAd('左侧大图')
      data.rightAd = findAd('右侧大图')
      data.centerDownAd = findAd('中心小图')
      data.leftDownAd = findAd('左侧小图')
      data.rightDownAd = findAd('右侧小图')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const findAd = (location) => {
  return data.advertiseData.find(v => v.location === location) || {}
}

const loadIndustry = () => {
  request.get('/industry/selectAll').then(res => {
    if (res.code === '200') {
      data.industryData = res.data || []
      if (data.industryData.length > 0) {
        data.activeName = data.industryData[0].id
        handleClick(data.activeName)
      }
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleClick = (industryId) => {
  request.get('/position/selectAll', {
    params: {
      industryId: industryId,
      status: '审核通过'
    }
  }).then(res => {
    if (res.code === '200') {
      data.positionData = res.data || []
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const navToAd = (ad) => {
  if (ad && ad.positionId) {
    navTo('/front/positionDetail?id=' + ad.positionId)
  }
}

const navTo = (url) => {
  location.href = url
}

const search = () => {
  const keyword = data.name ? data.name.trim() : ''
  location.href = '/front/search?name=' + encodeURIComponent(keyword)
}

loadAdvertise()
loadIndustry()
</script>

<style scoped>
.talent-home {
  min-height: 100vh;
  color: #e9f7ff;
  background:
      linear-gradient(135deg, rgba(7, 22, 39, .96), rgba(13, 35, 51, .9) 42%, rgba(18, 45, 43, .92)),
      repeating-linear-gradient(90deg, rgba(93, 231, 255, .08) 0 1px, transparent 1px 96px);
  padding: 28px max(24px, 7vw) 56px;
}

.hero-shell {
  display: grid;
  grid-template-columns: minmax(320px, .85fr) minmax(420px, 1.15fr);
  gap: 28px;
  align-items: stretch;
  max-width: 1280px;
  margin: 0 auto;
}

.hero-copy,
.radar-panel,
.job-card {
  border: 1px solid rgba(112, 229, 255, .22);
  background: rgba(6, 18, 31, .72);
  box-shadow: 0 20px 60px rgba(0, 0, 0, .28);
  backdrop-filter: blur(16px);
}

.hero-copy {
  position: relative;
  overflow: hidden;
  min-height: 430px;
  padding: 42px;
  border-radius: 8px;
}

.hero-copy::after {
  position: absolute;
  right: 34px;
  bottom: 32px;
  width: 156px;
  height: 156px;
  border: 1px solid rgba(99, 239, 255, .28);
  border-radius: 50%;
  background:
      linear-gradient(transparent 48%, rgba(99, 239, 255, .2) 49% 51%, transparent 52%),
      linear-gradient(90deg, transparent 48%, rgba(99, 239, 255, .2) 49% 51%, transparent 52%);
  content: "";
}

.eyebrow {
  margin-bottom: 12px;
  color: #68f3ff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

h1,
h2,
h3,
p {
  margin: 0;
}

h1 {
  color: #ffffff;
  font-size: 56px;
  line-height: 1.05;
  font-weight: 800;
}

.hero-desc {
  max-width: 480px;
  margin-top: 18px;
  color: #b8ccda;
  font-size: 17px;
  line-height: 1.8;
}

.search-console {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 10px;
  margin-top: 34px;
  padding: 12px;
  border: 1px solid rgba(104, 243, 255, .24);
  border-radius: 8px;
  background: rgba(3, 10, 20, .62);
}

.search-console :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 6px;
  background: rgba(255, 255, 255, .08);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .08);
}

.search-console :deep(.el-input__inner) {
  color: #ffffff;
}

.ghost-action {
  border-color: rgba(104, 243, 255, .3);
  color: #d8fbff;
  background: rgba(104, 243, 255, .08);
}

.signal-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 34px;
}

.signal-item {
  min-height: 82px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, .11);
  border-radius: 8px;
  background: rgba(255, 255, 255, .06);
}

.signal-item span {
  display: block;
  color: #d6ff65;
  font-size: 25px;
  font-weight: 800;
}

.signal-item small {
  display: block;
  margin-top: 8px;
  color: #a6bac8;
}

.radar-panel {
  min-height: 430px;
  padding: 16px;
  border-radius: 8px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 34px;
  color: #9feaff;
  font-size: 13px;
  font-weight: 700;
}

.panel-header i {
  width: 90px;
  height: 6px;
  border-radius: 99px;
  background: linear-gradient(90deg, #68f3ff, #d6ff65, #ffbf4d);
}

.ad-grid {
  display: grid;
  grid-template-columns: 1fr 1.6fr 1fr;
  grid-template-rows: 240px 132px;
  gap: 8px;
}

.ad-cell {
  position: relative;
  overflow: hidden;
  padding: 0;
  border: 1px solid rgba(104, 243, 255, .18);
  border-radius: 8px;
  color: #80deea;
  background: rgba(255, 255, 255, .05);
  cursor: pointer;
}

.ad-cell img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: saturate(1.08) contrast(1.04);
  transition: transform .3s ease;
}

.ad-cell:hover img,
.job-card:hover {
  transform: translateY(-3px);
}

.ad-cell::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 48%, rgba(2, 10, 18, .46));
  content: "";
}

.ad-main,
.ad-wide {
  grid-column: span 1;
}

.position-section {
  max-width: 1280px;
  margin: 34px auto 0;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  margin-bottom: 16px;
}

h2 {
  color: #ffffff;
  font-size: 28px;
  font-weight: 800;
}

.industry-tabs {
  border: 1px solid rgba(104, 243, 255, .16);
  border-radius: 8px;
  background: rgba(5, 16, 28, .68);
  padding: 6px 16px 18px;
}

.industry-tabs :deep(.el-tabs__item) {
  color: #91a9b8;
  font-weight: 700;
}

.industry-tabs :deep(.el-tabs__item.is-active) {
  color: #68f3ff;
}

.industry-tabs :deep(.el-tabs__active-bar) {
  background: #68f3ff;
}

.job-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.job-card {
  min-height: 182px;
  padding: 18px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform .22s ease, border-color .22s ease, background .22s ease;
}

.job-card:hover {
  border-color: rgba(214, 255, 101, .55);
  background: rgba(9, 31, 44, .88);
}

.job-topline {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.job-topline h3 {
  color: #ffffff;
  font-size: 17px;
  font-weight: 800;
  line-height: 1.35;
}

.job-topline p {
  margin-top: 8px;
  color: #8ea7b6;
  font-size: 13px;
}

.job-topline strong {
  flex: 0 0 auto;
  color: #ffbf4d;
  font-size: 15px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 30px;
  margin-top: 18px;
}

.tag-list :deep(.el-tag) {
  border-color: rgba(104, 243, 255, .18);
  color: #d8fbff;
  background: rgba(104, 243, 255, .08);
}

.job-footer {
  display: grid;
  grid-template-columns: 32px 1fr auto 22px;
  gap: 10px;
  align-items: center;
  margin-top: 18px;
  color: #a7bcc9;
  font-size: 13px;
}

.job-footer img {
  width: 32px;
  height: 32px;
  border: 1px solid rgba(104, 243, 255, .28);
  border-radius: 7px;
  object-fit: cover;
}

.job-footer .el-icon {
  color: #d6ff65;
}

@media (max-width: 1080px) {
  .hero-shell {
    grid-template-columns: 1fr;
  }

  .job-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .talent-home {
    padding: 18px 14px 34px;
  }

  .hero-copy {
    min-height: auto;
    padding: 26px;
  }

  h1 {
    font-size: 42px;
  }

  .search-console,
  .job-grid {
    grid-template-columns: 1fr;
  }

  .search-console {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .search-console :deep(.el-input) {
    grid-column: 1 / -1;
  }

  .signal-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px;
  }

  .signal-item {
    min-height: 68px;
    padding: 10px;
  }

  .signal-item span {
    font-size: 20px;
  }

  .signal-item small {
    font-size: 12px;
  }

  .ad-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    grid-template-rows: repeat(3, 100px);
  }

  .radar-panel {
    min-height: auto;
  }

  .section-title {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
