package dev.resteasy.grpc.example;

public class InnerClasses {

    public static class PublicPublic {
        public int i;

        public PublicPublic() {
        }

        public PublicPublic(int i) {
            this.i = i;
        }

        public boolean equals(Object other) {
            if (!PublicPublic.class.equals(other.getClass())) {
                return false;
            }
            PublicPublic stuff = (PublicPublic) other;
            return this.i == stuff.i;
        }
    }

    public static class PublicPrivate {
        public int i;

        private PublicPrivate() {
        }

        private PublicPrivate(int i) {
            this.i = i;
        }

        public boolean equals(Object other) {
            if (!PublicPrivate.class.equals(other.getClass())) {
                return false;
            }
            PublicPrivate stuff = (PublicPrivate) other;
            return this.i == stuff.i;
        }
    }

    private static class PrivatePublic {
        public int i;

        public PrivatePublic() {
        }

        public PrivatePublic(int i) {
            this.i = i;
        }

        public boolean equals(Object other) {
            if (!PrivatePublic.class.equals(other.getClass())) {
                return false;
            }
            PrivatePublic stuff = (PrivatePublic) other;
            return this.i == stuff.i;
        }
    }

    private static class PrivatePrivate {
        public int i;

        private PrivatePrivate() {
        }

        private PrivatePrivate(int i) {
            this.i = i;
        }

        public boolean equals(Object other) {
            if (!PrivatePrivate.class.equals(other.getClass())) {
                return false;
            }
            PrivatePrivate stuff = (PrivatePrivate) other;
            return this.i == stuff.i;
        }
    }

    public static class InnerClassHolder {

        private PublicPublic publicPublic;
        private PublicPrivate publicPrivate;
        private PrivatePublic privatePublic;
        private PrivatePrivate privatePrivate;

        public InnerClassHolder(int n) {
            publicPublic = new PublicPublic(n + 3);
            publicPrivate = new PublicPrivate(n + 5);
            privatePublic = new PrivatePublic(n + 7);
            privatePrivate = new PrivatePrivate(n + 11);
        }

        public boolean equals(Object object) {
        	if (object == null) {
        		return false;
        	}
            if (!InnerClassHolder.class.equals(object.getClass())) {
                return false;
            }
            InnerClassHolder ich = (InnerClassHolder) object;
            return this.publicPublic.i == ich.publicPublic.i
                    && this.publicPrivate.i == ich.publicPrivate.i
                    && this.privatePublic.i == ich.privatePublic.i
                    && this.privatePrivate.i == ich.privatePrivate.i;
        }
    }
}
