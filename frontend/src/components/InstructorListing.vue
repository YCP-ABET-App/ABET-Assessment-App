<script lang="ts" setup>
import { ref } from 'vue';
import api from '@/api'
import { BaseCard } from '@/components/ui'

const props = defineProps({ iid: Number })

const instructor_obj = ref({
    userId: NaN,
    firstName: '',
    lastName: '',
    email: ''
})
const link = ref('')

async function initialize() {
    //Fetch instructor name
    try {
        const { data } = await api.get(`/users/${props.iid}`);
        instructor_obj.value = {
            userId: data.data.userId,
            firstName: data.data.firstName,
            lastName: data.data.lastName,
            email: data.data.email,
        }
    } catch (error) {
        console.error('Error fetching or parsing instructor data:', error);
    }

    link.value = `/instructor/${props.iid}`
}

initialize()
</script>

<template>
    <router-link :to="`/instructor/${props.iid}`" class="instructor-link">
        <BaseCard variant="elevated" hoverable class="mini-card">
            <div class="instructor-card-content">
                <div class="instructor-avatar">
                    {{ instructor_obj.firstName?.charAt(0) }}{{ instructor_obj.lastName?.charAt(0) }}
                </div>

                <div class="instructor-info">
                    <h3 class="instructor-name">
                        {{ instructor_obj.firstName }} {{ instructor_obj.lastName }}
                    </h3>
                    <p class="instructor-email">
                        {{ instructor_obj.email }}
                    </p>
                </div>
            </div>
        </BaseCard>
    </router-link>
</template>

<style>
.instructor-link {
    text-decoration: none;
}

.instructor-card {
    cursor: pointer;
    transition: all 0.2s ease;
}

.instructor-card-content {
    display: flex;
    align-items: center;
    text-align: left;
    gap: 1.25rem;
    text-decoration: none;
}

.instructor-avatar {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: var(--color-primary);

    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    font-weight: 600;
    flex-shrink: 0;
}

.instructor-info {
    flex: 1;
    min-width: 0;
}

.instructor-name {
    margin: 0 0 0.25rem 0;
    font-size: 1.125rem;
    font-weight: 600;
    color: var(--color-text-primary);
}

.instructor-email {
    margin: 0 0 0.5rem 0;
    font-size: 0.875rem;
    color: var(--color-text-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
</style>
