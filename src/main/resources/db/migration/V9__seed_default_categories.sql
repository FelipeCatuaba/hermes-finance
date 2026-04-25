INSERT INTO expense_categories (id, user_id, name, icon, color_hex, is_default)
VALUES
  (gen_random_uuid(), NULL, 'Alimentacao', 'utensils', '#EF4444', TRUE),
  (gen_random_uuid(), NULL, 'Moradia', 'home', '#3B82F6', TRUE),
  (gen_random_uuid(), NULL, 'Transporte', 'car', '#14B8A6', TRUE),
  (gen_random_uuid(), NULL, 'Saude', 'heart-pulse', '#F97316', TRUE),
  (gen_random_uuid(), NULL, 'Lazer', 'party-popper', '#A855F7', TRUE),
  (gen_random_uuid(), NULL, 'Outros', 'shapes', '#64748B', TRUE);
