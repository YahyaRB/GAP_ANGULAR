import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteOFComponent } from './delete-of.component';

describe('DeleteOFComponent', () => {
  let component: DeleteOFComponent;
  let fixture: ComponentFixture<DeleteOFComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DeleteOFComponent]
    });
    fixture = TestBed.createComponent(DeleteOFComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
