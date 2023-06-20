package jua.compiler;

public final class Name {

    /**
     * Позиция идентификатора в исходном коде.
     * Это мета-информационное поле, оно не участвует в жизни объекта.
     */
    public final int pos;

    /**
     * Значение идентификатора.
     */
    public final String content;

    public Name(int pos, String content) {
        this.pos = pos;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Name n = (Name) o;
        // pos сравнивать не нужно
        return content.equals(n.content);
    }

    @Override
    public int hashCode() {
        // pos в хеш входить не должен
        return content.hashCode();
    }

    @Override
    public String toString() {
        return content;
    }
}
