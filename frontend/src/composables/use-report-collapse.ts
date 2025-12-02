import { reactive } from "vue";

// One shared collapse store for the whole report system
const collapsed = reactive<Record<string | number, boolean>>({});

function isCollapsed(id: string | number) {
  return collapsed[id] ?? false;
}

function toggle(id: string | number) {
  collapsed[id] = !collapsed[id];
}

function expandAll(ids: (string | number)[]) {
  ids.forEach(id => (collapsed[id] = false));
}

function collapseAll(ids: (string | number)[]) {
  ids.forEach(id => (collapsed[id] = true));
}

export function useReportCollapse() {
  return {
    collapsed,
    isCollapsed,
    toggle,
    expandAll,
    collapseAll,
  };
}
