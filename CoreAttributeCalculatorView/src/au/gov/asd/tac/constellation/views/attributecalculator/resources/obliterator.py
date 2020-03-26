# This special python singleton object is used to represent null values for attributes inside the attribute calculator.
# It is designed to be equal to the inbuilt singleton None, and behaves like the object False
# in conditional expressions (if) and boolean expressions (and or not).
# If this singleton object is the result of an attribute calculator evaluation, then the plugin
# will store the java representation of null (rather than this object) in the destination attribute.

class Obliterator(object):

    the_obliterator = None

    def __new__(cls):
        if Obliterator.the_obliterator is None:
            Obliterator.the_obliterator = super(Obliterator, cls).__new__(cls)
        return Obliterator.the_obliterator

    def __init__(self):
        pass

    def __getattr__(self, name):
        return lambda : self

    def __getattribute__(self, name):
        return lambda : self

    def __eq__(self, other):
        if other is None or other is self:
            return True
        return False

    def __lt__(self, other):
        return self

    def __le__(self, other):
        return self

    def __ne__(self, other):
        return not self.__eq__(other)

    def __gt__(self, other):
        return self

    def __ge__(self, other):
        return self

    def __setattr__(self, name, val):
        pass

    def __getitem__(self, key):
        return self

    def __setitem__(self, key, val):
        pass

    def __contains__(self, item):
        return False

    def __add__(self, other):
        return self

    def __sub__(self, other):
        return self

    def __mul__(self, other):
        return self

    def __truediv__(self, other):
        return self

    def __floordiv__(self, other):
        return self

    def __mod__(self, other):
        return self

    def __divmod__(self, other):
        return self

    def __pow__(self, other):
        return self

    def __lshift__(self, other):
        return self

    def __rshift__(self, other):
        return self

    def __and__(self, other):
        return self

    def __or__(self, other):
        return self

    def __xor__(self, other):
        return self

    def __radd__(self, other):
        return self

    def __rsub__(self, other):
        return self

    def __rmul__(self, other):
        return self

    def __rtruediv__(self, other):
        return self

    def __rfloordiv__(self, other):
        return self

    def __mod__(self, other):
        return self

    def __rdivmod__(self, other):
        return self

    def __rpow__(self, other):
        return self

    def __rlshift__(self, other):
        return self

    def __rrshift__(self, other):
        return self

    def __rand__(self, other):
        return self

    def __ror__(self, other):
        return self

    def __rxor__(self, other):
        return self

    def __int__(self):
        return 0

    def __float__(self):
        return 0

    def __len__(self):
        return 0

    def __nonzero__(self):
        return False

    def __hash__(self):
        return None.__hash__()-1

    def __repr__(self):
        return ''

    def __str__(self):
        return ''