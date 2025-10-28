import Header from "@/components/Header";
import Hero from "@/components/Hero";
import Categories from "@/components/Categories";
import FeaturedProducts from "@/components/FeaturedProducts";
import Footer from "@/components/Footer";
import TradeRequests from "@/components/TradeRequests";

const Index = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        <Hero />
        <Categories />
        <FeaturedProducts />
        <section className="py-16 bg-muted/40">
          <div className="container mx-auto px-4">
            <TradeRequests />
          </div>
        </section>
      </main>
      <Footer />
    </div>
  );
};

export default Index;
