<template>
  <router-view />
</template>

<style>
/* 全局重置样式 */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  background-color: #f0f2f5;
  overflow: hidden; /* 防止最外层出现滚动条 */
}

#app {
  width: 100%;
  height: 100%;
}

/* 修复 Element Plus 某些组件可能导致的盒模型问题 */
* {
  box-sizing: border-box;
}
</style>