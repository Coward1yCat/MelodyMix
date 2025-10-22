<template>
  <div>
    <h1>Welcome to MelodyMix!</h1>
    <p>This is the home page. Please log in or register.</p>
    <router-link to="/login">Go to Login</router-link> |
    <router-link to="/register">Go to Register</router-link>
  </div>
</template>

<script setup>
// 可以在这里添加组件逻辑
</script>

<style scoped>
div {
  text-align: center;
  margin-top: 50px;
}
</style>