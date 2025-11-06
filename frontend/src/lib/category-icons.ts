import {
  BookOpen,
  Car,
  Dumbbell,
  Home,
  Palette,
  Shirt,
  Smartphone,
  Tag,
  Wrench,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

const CATEGORY_ICON_MAP: Record<string, LucideIcon> = {
  "lucide:book-open": BookOpen,
  "lucide:shirt": Shirt,
  "lucide:smartphone": Smartphone,
  "lucide:home": Home,
  "lucide:car": Car,
  "lucide:palette": Palette,
  "lucide:dumbbell": Dumbbell,
  "lucide:wrench": Wrench,
};

export const getCategoryIcon = (iconKey: string | null | undefined): LucideIcon => {
  if (!iconKey) {
    return Tag;
  }

  return CATEGORY_ICON_MAP[iconKey] ?? Tag;
};
