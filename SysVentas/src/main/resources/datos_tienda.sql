-- =====================================================================
-- MODA STORE - datos de la tienda de ropa (se ejecuta en cada arranque)
-- Cada sentencia es idempotente y el script es tolerante a errores.
-- =====================================================================

-- 1) Columnas propias de la ropa
ALTER TABLE producto ADD COLUMN IF NOT EXISTS talla VARCHAR(10) DEFAULT 'M';
ALTER TABLE producto ADD COLUMN IF NOT EXISTS color VARCHAR(20) DEFAULT 'Negro';

-- 2) Perfil y usuarios
MERGE INTO perfil (id_perfil, nombre, codigo) KEY (id_perfil) VALUES (4, 'Vendedor', 'VEN');
MERGE INTO usuario (id_usuario, usuario, clave, estado, id_perfil) KEY (id_usuario) VALUES (10, 'vendedor', 'vendedor123', 'ACTIVO', 4);
-- Usuario interno: figura como emisor de las compras hechas por el comprador (no se usa para iniciar sesión)
MERGE INTO usuario (id_usuario, usuario, clave, estado, id_perfil) KEY (id_usuario) VALUES (12, 'tienda_online', 'x9Qe-sin-acceso-7Lw2', 'ACTIVO', 3);

-- 3) Catálogo de ejemplo (solo se inserta si el producto no existe)
INSERT INTO producto (nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, tipo_producto, talla, color) SELECT 'Polo Básico Algodón', 35, 35, 12, 40, 40, 1, 1, 1, 'PRODUCTO', 'M', 'Blanco' WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Polo Básico Algodón');
INSERT INTO producto (nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, tipo_producto, talla, color) SELECT 'Polo Oversize Estampado', 49.9, 49.9, 18, 25, 25, 1, 2, 1, 'PRODUCTO', 'L', 'Negro' WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Polo Oversize Estampado');
INSERT INTO producto (nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, tipo_producto, talla, color) SELECT 'Jean Slim Fit', 119.9, 119.9, 40, 18, 18, 2, 4, 1, 'PRODUCTO', '32', 'Azul' WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Jean Slim Fit');
INSERT INTO producto (nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, tipo_producto, talla, color) SELECT 'Pantalón Jogger', 79.9, 79.9, 25, 30, 30, 2, 3, 1, 'PRODUCTO', 'M', 'Gris' WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Pantalón Jogger');
INSERT INTO producto (nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, tipo_producto, talla, color) SELECT 'Casaca Denim', 159, 159, 55, 12, 12, 3, 4, 1, 'PRODUCTO', 'L', 'Celeste' WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Casaca Denim');
INSERT INTO producto (nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, tipo_producto, talla, color) SELECT 'Casaca Cortaviento', 129.9, 129.9, 45, 15, 15, 3, 2, 1, 'PRODUCTO', 'M', 'Rojo' WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Casaca Cortaviento');
INSERT INTO producto (nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, tipo_producto, talla, color) SELECT 'Vestido Floral Verano', 89.9, 89.9, 30, 20, 20, 4, 1, 1, 'PRODUCTO', 'S', 'Rosado' WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Vestido Floral Verano');
INSERT INTO producto (nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, tipo_producto, talla, color) SELECT 'Zapatillas Urbanas', 219, 219, 70, 10, 10, 5, 3, 1, 'PRODUCTO', '40', 'Blanco' WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Zapatillas Urbanas');
INSERT INTO producto (nombre, pu, puold, utilidad, stock, stockold, id_categoria, id_marca, id_unidad, tipo_producto, talla, color) SELECT 'Zapatillas Running', 249.9, 249.9, 80, 4, 4, 5, 2, 1, 'PRODUCTO', '42', 'Negro' WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Zapatillas Running');
