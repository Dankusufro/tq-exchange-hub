import ProductCard from "./ProductCard";
import { Button } from "@/components/ui/button";
import { ArrowRight } from "lucide-react";
import bikeImage from "@/assets/product-bike.jpg";
import jacketImage from "@/assets/product-jacket.jpg";
import booksImage from "@/assets/product-books.jpg";

const featuredProducts = [
  {
    id: "1",
    title: "Bicicleta de Montaña Trek",
    description: "Bicicleta en excelente estado, perfecta para aventuras. Cambios Shimano, frenos de disco.",
    image: bikeImage,
    category: "Deportes",
    condition: "Muy bueno",
    location: "Ciudad de México",
    userRating: 4.8,
    userName: "Carlos M.",
    lookingFor: ["Laptop", "Guitarra", "Cámara fotográfica"]
  },
  {
    id: "2", 
    title: "Chaqueta de Cuero Vintage",
    description: "Auténtica chaqueta de cuero vintage, talla M. Perfecta para completar tu look rockero.",
    image: jacketImage,
    category: "Ropa",
    condition: "Excelente",
    location: "Guadalajara",
    userRating: 4.9,
    userName: "Ana L.",
    lookingFor: ["Botas", "Perfume", "Reloj"]
  },
  {
    id: "3",
    title: "Libros Universitarios",
    description: "Colección de libros de ingeniería y matemáticas. Perfectos para estudiantes.",
    image: booksImage,
    category: "Educación",
    condition: "Bueno",
    location: "Monterrey",
    userRating: 4.7,
    userName: "Miguel R.",
    lookingFor: ["Tablet", "Auriculares", "Mochila"]
  },
  {
    id: "4",
    title: "Clases de Yoga",
    description: "Instructora certificada ofrece clases personalizadas de yoga y meditación.",
    image: bikeImage, // temporal, se puede cambiar por una imagen de yoga
    category: "Servicios",
    condition: "Nuevo",
    location: "Puebla",
    userRating: 5.0,
    userName: "Sofia V.",
    lookingFor: ["Masajes", "Clases de cocina", "Consultas"]
  },
  {
    id: "5",
    title: "iPhone 12 Pro",
    description: "iPhone en perfecto estado, batería al 89%. Incluye cargador y funda.",
    image: jacketImage, // temporal
    category: "Electrónicos",
    condition: "Muy bueno",
    location: "Tijuana",
    userRating: 4.6,
    userName: "Roberto P.",
    lookingFor: ["Laptop", "Consola", "Bicicleta"]
  },
  {
    id: "6",
    title: "Plantas de Interior",
    description: "Hermosas plantas para decorar tu hogar. Incluye macetas y cuidados básicos.",
    image: booksImage, // temporal
    category: "Hogar",
    condition: "Excelente",
    location: "Mérida",
    userRating: 4.8,
    userName: "Laura G.",
    lookingFor: ["Libros", "Ropa", "Accesorios"]
  }
];

const FeaturedProducts = () => {
  return (
    <section className="py-16 bg-background">
      <div className="container mx-auto px-4">
        <div className="text-center space-y-4 mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-foreground">
            Intercambios destacados
          </h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Descubre los productos y servicios más populares de nuestra comunidad
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">
          {featuredProducts.map((product) => (
            <ProductCard key={product.id} {...product} />
          ))}
        </div>

        <div className="text-center">
          <Button size="lg" className="px-8">
            Ver todos los productos
            <ArrowRight className="ml-2 h-4 w-4" />
          </Button>
        </div>
      </div>
    </section>
  );
};

export default FeaturedProducts;