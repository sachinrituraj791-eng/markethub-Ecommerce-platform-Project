import type {Metadata} from 'next';
import './globals.css'; // Global styles

export const metadata: Metadata = {
  title: 'MarketHub - Enterprise E-Commerce Platform',
  description: 'Enterprise-grade online e-commerce platform featuring multi-role Buyer, Seller, and Admin portals, robust catalog, cart, transactional checkout, and comprehensive GUVI evaluation architecture.',
  openGraph: {
    title: 'MarketHub - Enterprise E-Commerce Platform',
    description: 'Enterprise-grade online e-commerce platform featuring multi-role Buyer, Seller, and Admin portals, robust catalog, cart, transactional checkout, and comprehensive GUVI evaluation architecture.',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'MarketHub - Enterprise E-Commerce Platform',
    description: 'Enterprise-grade online e-commerce platform featuring multi-role Buyer, Seller, and Admin portals, robust catalog, cart, transactional checkout, and comprehensive GUVI evaluation architecture.',
  },
};

export default function RootLayout({children}: {children: React.ReactNode}) {
  return (
    <html lang="en">
      <body suppressHydrationWarning>{children}</body>
    </html>
  );
}
